package com.moradi.topicclassificationjava.elasticwrapper;

import org.apache.commons.csv.CSVRecord;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder.Item;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
@Import(ElasticConfig.class)
public class ElasticHelper {
    private final RestHighLevelClient client;
    private final Map<String, Double> zeroMatching;

    public ElasticHelper(@Autowired RestHighLevelClient client) {
        this.client = client;
        this.zeroMatching = new HashMap<>();
        this.zeroMatching.put("match_prob", 0.0);
    }

    public boolean indexExists(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return this.client.indices().exists(request, RequestOptions.DEFAULT);
    }

    public void createIndex(String indexName, Map<String, Object> fields) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.settings(Settings.builder().put("index.number_of_replicas", 5));
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", fields);
        request.mapping(mapping);
        this.client.indices().create(request, RequestOptions.DEFAULT);
    }

    public void deleteIndex(String indexName) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        this.client.indices().delete(request, RequestOptions.DEFAULT);
    }

    public void bulkIndexDocuments(
            String indexName, List<String> headerNames, List<CSVRecord> records) throws IOException {
        BulkRequest request = new BulkRequest();
        records.stream()
                .map(
                        rec -> {
                            Map<String, Object> jsonMap = new HashMap<>();
                            headerNames.forEach(name -> jsonMap.put(name, rec.get(name)));
                            return new IndexRequest(indexName).source(jsonMap);
                        })
                .forEach(request::add);
        BulkResponse bulk = this.client.bulk(request, RequestOptions.DEFAULT);
        if (bulk.hasFailures()) {
            throw new RuntimeException("Problem with document indexing");
        }
    }

    public List<Object> moreLikeThisBulk(String indexName, List<String> documents)
            throws IOException {
        XContentBuilder builder = JsonXContent.contentBuilder();
        builder.startObject();
        builder.field("doc", "");
        builder.field("target", "");
        builder.endObject();
        MultiSearchRequest request = new MultiSearchRequest();
        documents.stream()
                .map(
                        doc -> {
                            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                            searchSourceBuilder.query(
                                    QueryBuilders.moreLikeThisQuery(
                                            new String[]{"doc"},
                                            new String[]{doc},
                                            new Item[]{new Item(indexName, builder)}));
                            return new SearchRequest(indexName).source(searchSourceBuilder);
                        })
                .forEach(request::add);

        MultiSearchResponse responses = client.msearch(request, RequestOptions.DEFAULT);
        return parseMultiSearchResponse(responses);
    }

    private List<Object> parseMultiSearchResponse(MultiSearchResponse responses) {
        List<Object> responseList = new LinkedList<>();
        for (MultiSearchResponse.Item response : responses.getResponses()) {
            if (!response.isFailure()) {
                SearchHit[] hits = response.getResponse().getHits().getHits();
                Map<String, Map<String, Double>> answer = new HashMap<>();
                if (Stream.of(hits).allMatch(hit -> hit.getSourceAsMap().get("target").equals("other"))) {
                    answer.put("nudity", zeroMatching);
                    answer.put("weapons", zeroMatching);
                } else if (Stream.of(hits)
                        .allMatch(hit -> hit.getSourceAsMap().get("target").equals("weapons"))) {
                    answer.put("nudity", zeroMatching);
                    Map<String, Double> weaponsResponse = new HashMap<>();
                    weaponsResponse.put("match_prob", 0.97);
                    answer.put("weapons", weaponsResponse);
                } else if (Stream.of(hits)
                        .allMatch(hit -> hit.getSourceAsMap().get("target").equals("nudity"))) {
                    answer.put("weapons", zeroMatching);
                    Map<String, Double> nudityResponse = new HashMap<>();
                    nudityResponse.put("match_prob", 0.97);
                    answer.put("nudity", nudityResponse);
                } else {
                    double weaponsAvgScore =
                            Stream.of(hits)
                                    .filter(hit -> hit.getSourceAsMap().get("target").equals("weapons"))
                                    .mapToDouble(SearchHit::getScore)
                                    .sum()
                                    / (double) hits.length;
                    double nudityAvgScore =
                            Stream.of(hits)
                                    .filter(hit -> hit.getSourceAsMap().get("target").equals("nudity"))
                                    .mapToDouble(SearchHit::getScore)
                                    .sum()
                                    / (double) hits.length;
                    double otherAvgScore =
                            Stream.of(hits)
                                    .filter(hit -> hit.getSourceAsMap().get("target").equals("other"))
                                    .mapToDouble(SearchHit::getScore)
                                    .sum()
                                    / (double) hits.length;
                    double maxScore =
                            Stream.of(weaponsAvgScore, nudityAvgScore, otherAvgScore)
                                    .max(Double::compareTo)
                                    .orElse(0.0)
                                    + 0.5;
                    double minScore =
                            Stream.of(weaponsAvgScore, nudityAvgScore, otherAvgScore)
                                    .min(Double::compareTo)
                                    .orElse(0.0);

                    Map<String, Double> weaponsResponse = new HashMap<>();
                    weaponsResponse.put("match_prob", (weaponsAvgScore - minScore) / (maxScore - minScore));
                    answer.put("weapons", weaponsResponse);
                    Map<String, Double> nudityResponse = new HashMap<>();
                    nudityResponse.put("match_prob", (nudityAvgScore - minScore) / (maxScore - minScore));
                    answer.put("nudity", nudityResponse);
                }
                responseList.add(answer);
            } else {
                Map<String, String> failedMap = new HashMap<>();
                failedMap.put("Failed to evaluate", response.getFailureMessage());
                responseList.add(failedMap);
            }
        }
        return responseList;
    }

    public boolean checkHealth(String indexName) throws IOException {
        try {
            ClusterHealthRequest request = new ClusterHealthRequest(indexName);
            client.cluster().health(request, RequestOptions.DEFAULT);
        } catch (ConnectException ignored) {
            return false;
        }
        return true;
    }
}
