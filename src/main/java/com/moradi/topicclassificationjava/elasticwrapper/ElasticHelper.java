package com.moradi.topicclassificationjava.elasticwrapper;

import com.moradi.topicclassificationjava.categories.Categories;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Import(ElasticConfig.class)
public class ElasticHelper {

  private final RestHighLevelClient client;
  private static final double ZERO_MATCHING = 0;
  private static final double MAX_MATCHING = 0.97;

  public ElasticHelper(@Autowired RestHighLevelClient client) {
    this.client = client;
  }

  public boolean indexExists(String indexName) throws IOException {
    GetIndexRequest request = new GetIndexRequest(indexName);
    return this.client.indices().exists(request, RequestOptions.DEFAULT);
  }

  public void createIndex(
      String indexName, Map<String, Object> mapping, Map<String, Object> settings)
      throws IOException {
    CreateIndexRequest request = new CreateIndexRequest(indexName);
    request.settings(settings);
    request.mapping(mapping);
    this.client.indices().create(request, RequestOptions.DEFAULT);
  }

  public void deleteIndex(String indexName) throws IOException {
    DeleteIndexRequest request = new DeleteIndexRequest(indexName);
    this.client.indices().delete(request, RequestOptions.DEFAULT);
  }

  public void bulkIndexDocuments(String indexName, List<CSVRecord> records) throws IOException {
    BulkRequest request = new BulkRequest();
    records.stream()
        .map(rec -> new IndexRequest(indexName).source(rec.toMap()))
        .forEach(request::add);
    BulkResponse bulk = this.client.bulk(request, RequestOptions.DEFAULT);
    if (bulk.hasFailures()) {
      throw new RuntimeException("Problem with document indexing");
    }
  }

  public List<Map<Categories, Double>> moreLikeThisBulk(String indexName, List<String> documents)
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
                      new String[] {"doc"},
                      new String[] {doc},
                      new Item[] {new Item(indexName, builder)}));
              return new SearchRequest(indexName).source(searchSourceBuilder);
            })
        .forEach(request::add);

    MultiSearchResponse responses = client.msearch(request, RequestOptions.DEFAULT);
    return parseMultiSearchResponse(responses);
  }

  private List<Map<Categories, Double>> parseMultiSearchResponse(MultiSearchResponse responses) {
    List<Map<Categories, Double>> responseList = new LinkedList<>();
    for (MultiSearchResponse.Item response : responses.getResponses()) {
      if (!response.isFailure()) {
        SearchHit[] hits = response.getResponse().getHits().getHits();
        double maxScore = Stream.of(hits).mapToDouble(SearchHit::getScore).max().orElse(0.0);
        double minScore = 0;

        Map<Categories, Double> answer = new HashMap<>();
        for (Categories category : Categories.values()) {
          if (hits.length > 0
              && Stream.of(hits)
                  .allMatch(hit -> hit.getSourceAsMap().get("target").equals(category.getText()))) {
            answer.put(category, MAX_MATCHING);
          } else {
            double avgScore = 0.0;
            if (hits.length > 0) {
              avgScore =
                  Stream.of(hits)
                          .filter(
                              hit -> hit.getSourceAsMap().get("target").equals(category.getText()))
                          .mapToDouble(SearchHit::getScore)
                          .sum()
                      / (double) hits.length;
            }
            if (avgScore == 0.0) {
              answer.put(category, ZERO_MATCHING);
            } else {
              answer.put(category, (avgScore - minScore) / (maxScore - minScore));
            }
          }
        }
        responseList.add(answer);
      } else {
        responseList.add(
            Stream.of(Categories.values())
                .collect(Collectors.toMap(Function.identity(), val -> 0.0)));
      }
    }
    return responseList;
  }

  public boolean checkHealth(String indexName) {
    try {
      ClusterHealthRequest request = new ClusterHealthRequest(indexName);
      client.cluster().health(request, RequestOptions.DEFAULT);
    } catch (ConnectException ignored) {
      return false;
    } catch (IOException ignored) {
      return true;
    }
    return true;
  }
}
