package com.moradi.topicclassificationjava.elasticwrapper;

import com.moradi.topicclassificationjava.categories.Categories;
import org.apache.commons.csv.CSVRecord;
import org.assertj.core.util.Lists;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ClusterClient;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.ConnectException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@RunWith(MockitoJUnitRunner.class)
public class ElasticHelperTest {

  @Mock private RestHighLevelClient client;

  private ElasticHelper elasticHelper;

  @Before
  public void setUp() {
    elasticHelper = new ElasticHelper(client);
  }

  @Test
  public void indexExists() throws Exception {
    String indexName = "demo";
    IndicesClient indicesClient = Mockito.mock(IndicesClient.class);
    Mockito.when(client.indices()).thenReturn(indicesClient);
    Mockito.when(
            indicesClient.exists(
                (GetIndexRequest) Mockito.any(), Mockito.eq(RequestOptions.DEFAULT)))
        .thenReturn(true);
    Assert.assertTrue(elasticHelper.indexExists(indexName));
  }

  @Test
  public void createIndex() throws Exception {
    String indexName = "demo";
    Map<String, Object> fields = new HashMap<>();

    IndicesClient indicesClient = Mockito.mock(IndicesClient.class);
    Mockito.when(client.indices()).thenReturn(indicesClient);
    elasticHelper.createIndex(indexName, fields, fields);
    Mockito.verify(indicesClient, Mockito.times(1))
        .create((CreateIndexRequest) Mockito.any(), Mockito.eq(RequestOptions.DEFAULT));
  }

  @Test
  public void deleteIndex() throws Exception {
    String indexName = "demo";

    IndicesClient indicesClient = Mockito.mock(IndicesClient.class);
    Mockito.when(client.indices()).thenReturn(indicesClient);
    elasticHelper.deleteIndex(indexName);
    Mockito.verify(indicesClient, Mockito.times(1))
        .delete(Mockito.any(), Mockito.eq(RequestOptions.DEFAULT));
  }

  @Test
  public void bulkIndexDocuments() throws Exception {
    String indexName = "demo";
    CSVRecord rec = Mockito.mock(CSVRecord.class);
    Mockito.when(rec.toMap()).thenReturn(new HashMap<>());
    BulkResponse bulk = Mockito.mock(BulkResponse.class);
    Mockito.when(bulk.hasFailures()).thenReturn(false);

    Mockito.when(client.bulk(Mockito.any(), Mockito.eq(RequestOptions.DEFAULT))).thenReturn(bulk);
    elasticHelper.bulkIndexDocuments(indexName, Collections.singletonList(rec));
  }

  @Test(expected = RuntimeException.class)
  public void bulkIndexDocumentsFailure() throws Exception {
    String indexName = "demo";
    CSVRecord rec = Mockito.mock(CSVRecord.class);
    Mockito.when(rec.toMap()).thenReturn(new HashMap<>());
    BulkResponse bulk = Mockito.mock(BulkResponse.class);
    Mockito.when(bulk.hasFailures()).thenReturn(true);

    Mockito.when(client.bulk(Mockito.any(), Mockito.eq(RequestOptions.DEFAULT))).thenReturn(bulk);
    elasticHelper.bulkIndexDocuments(indexName, Collections.singletonList(rec));
  }

  @Test
  public void moreLikeThisBulkFailure() throws Exception {
    String indexName = "demo";
    String document = "small document";
    List<String> documents = Collections.singletonList(document);

    MultiSearchResponse responses = Mockito.mock(MultiSearchResponse.class);
    MultiSearchResponse.Item item = Mockito.mock(MultiSearchResponse.Item.class);
    Mockito.when(item.isFailure()).thenReturn(true);

    Mockito.when(responses.getResponses()).thenReturn(new MultiSearchResponse.Item[] {item});
    Mockito.when(client.msearch(Mockito.any(), Mockito.eq(RequestOptions.DEFAULT)))
        .thenReturn(responses);

    List<Map<Categories, Double>> expected =
        Lists.list(
            MapBuilder.<Categories, Double>newMapBuilder()
                .put(Categories.WEAPONS, 0.0)
                .put(Categories.NUDITY, 0.0)
                .put(Categories.CYBER, 0.0)
                .put(Categories.DRUGS, 0.0)
                .map());

    Assert.assertEquals(expected, elasticHelper.moreLikeThisBulk(indexName, documents));
  }

  @Test
  public void moreLikeThisBulkAllOther() throws Exception {
    String indexName = "demo";
    String document = "small document";
    List<String> documents = Collections.singletonList(document);

    MultiSearchResponse responses = Mockito.mock(MultiSearchResponse.class);
    MultiSearchResponse.Item item = Mockito.mock(MultiSearchResponse.Item.class);
    Mockito.when(responses.getResponses()).thenReturn(new MultiSearchResponse.Item[] {item});

    Mockito.when(item.isFailure()).thenReturn(false);
    SearchResponse searchResponse = Mockito.mock(SearchResponse.class);
    SearchHits searchHits = Mockito.mock(SearchHits.class);
    Mockito.when(item.getResponse()).thenReturn(searchResponse);
    Mockito.when(searchResponse.getHits()).thenReturn(searchHits);
    SearchHit[] hits =
        IntStream.range(0, 10)
            .boxed()
            .map(
                i -> {
                  SearchHit hit = Mockito.mock(SearchHit.class);
                  Map<String, Object> sourceAsMap = new HashMap<>();
                  sourceAsMap.put("target", "other");
                  Mockito.when(hit.getSourceAsMap()).thenReturn(sourceAsMap);
                  return hit;
                })
            .toArray(SearchHit[]::new);
    Mockito.when(searchHits.getHits()).thenReturn(hits);
    Mockito.when(client.msearch(Mockito.any(), Mockito.eq(RequestOptions.DEFAULT)))
        .thenReturn(responses);

    List<Map<Categories, Double>> expected =
        Lists.list(
            MapBuilder.<Categories, Double>newMapBuilder()
                .put(Categories.WEAPONS, 0.0)
                .put(Categories.NUDITY, 0.0)
                .put(Categories.CYBER, 0.0)
                .put(Categories.DRUGS, 0.0)
                .map());

    Assert.assertEquals(expected, elasticHelper.moreLikeThisBulk(indexName, documents));
  }

  @Test
  public void moreLikeThisBulkAllWeapons() throws Exception {
    String indexName = "demo";
    String document = "small document";
    List<String> documents = Collections.singletonList(document);

    MultiSearchResponse responses = Mockito.mock(MultiSearchResponse.class);
    MultiSearchResponse.Item item = Mockito.mock(MultiSearchResponse.Item.class);
    Mockito.when(responses.getResponses()).thenReturn(new MultiSearchResponse.Item[] {item});

    Mockito.when(item.isFailure()).thenReturn(false);
    SearchResponse searchResponse = Mockito.mock(SearchResponse.class);
    SearchHits searchHits = Mockito.mock(SearchHits.class);
    Mockito.when(item.getResponse()).thenReturn(searchResponse);
    Mockito.when(searchResponse.getHits()).thenReturn(searchHits);
    SearchHit[] hits =
        IntStream.range(0, 10)
            .boxed()
            .map(
                i -> {
                  SearchHit hit = Mockito.mock(SearchHit.class);
                  Map<String, Object> sourceAsMap = new HashMap<>();
                  sourceAsMap.put("target", "weapons");
                  Mockito.when(hit.getSourceAsMap()).thenReturn(sourceAsMap);
                  return hit;
                })
            .toArray(SearchHit[]::new);
    Mockito.when(searchHits.getHits()).thenReturn(hits);
    Mockito.when(client.msearch(Mockito.any(), Mockito.eq(RequestOptions.DEFAULT)))
        .thenReturn(responses);

    List<Map<Categories, Double>> expected =
        Lists.list(
            MapBuilder.<Categories, Double>newMapBuilder()
                .put(Categories.WEAPONS, 0.97)
                .put(Categories.NUDITY, 0.0)
                .put(Categories.CYBER, 0.0)
                .put(Categories.DRUGS, 0.0)
                .map());

    Assert.assertEquals(expected, elasticHelper.moreLikeThisBulk(indexName, documents));
  }

  @Test
  public void moreLikeThisBulkAllNudity() throws Exception {
    String indexName = "demo";
    String document = "small document";
    List<String> documents = Collections.singletonList(document);

    MultiSearchResponse responses = Mockito.mock(MultiSearchResponse.class);
    MultiSearchResponse.Item item = Mockito.mock(MultiSearchResponse.Item.class);
    Mockito.when(responses.getResponses()).thenReturn(new MultiSearchResponse.Item[] {item});

    Mockito.when(item.isFailure()).thenReturn(false);
    SearchResponse searchResponse = Mockito.mock(SearchResponse.class);
    SearchHits searchHits = Mockito.mock(SearchHits.class);
    Mockito.when(item.getResponse()).thenReturn(searchResponse);
    Mockito.when(searchResponse.getHits()).thenReturn(searchHits);
    SearchHit[] hits =
        IntStream.range(0, 10)
            .boxed()
            .map(
                i -> {
                  SearchHit hit = Mockito.mock(SearchHit.class);
                  Map<String, Object> sourceAsMap = new HashMap<>();
                  sourceAsMap.put("target", "nudity");
                  Mockito.when(hit.getSourceAsMap()).thenReturn(sourceAsMap);
                  return hit;
                })
            .toArray(SearchHit[]::new);
    Mockito.when(searchHits.getHits()).thenReturn(hits);
    Mockito.when(client.msearch(Mockito.any(), Mockito.eq(RequestOptions.DEFAULT)))
        .thenReturn(responses);

    List<Map<Categories, Double>> expected =
        Lists.list(
            MapBuilder.<Categories, Double>newMapBuilder()
                .put(Categories.WEAPONS, 0.0)
                .put(Categories.NUDITY, 0.97)
                .put(Categories.CYBER, 0.0)
                .put(Categories.DRUGS, 0.0)
                .map());

    Assert.assertEquals(expected, elasticHelper.moreLikeThisBulk(indexName, documents));
  }

  @Test
  public void moreLikeThisBulk() throws Exception {
    String indexName = "demo";
    String document = "small document";
    List<String> documents = Collections.singletonList(document);

    MultiSearchResponse responses = Mockito.mock(MultiSearchResponse.class);
    MultiSearchResponse.Item item = Mockito.mock(MultiSearchResponse.Item.class);
    Mockito.when(responses.getResponses()).thenReturn(new MultiSearchResponse.Item[] {item});

    Mockito.when(item.isFailure()).thenReturn(false);
    SearchResponse searchResponse = Mockito.mock(SearchResponse.class);
    SearchHits searchHits = Mockito.mock(SearchHits.class);
    Mockito.when(item.getResponse()).thenReturn(searchResponse);
    Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

    SearchHit[] hits = new SearchHit[10];
    hits[0] = mockSearchHit("weapons", (float) 8.460396);
    hits[1] = mockSearchHit("weapons", (float) 8.21903);
    hits[2] = mockSearchHit("weapons", (float) 8.003758);
    hits[3] = mockSearchHit("weapons", (float) 7.990506);
    hits[4] = mockSearchHit("weapons", (float) 7.845314);
    hits[5] = mockSearchHit("weapons", (float) 7.8624835);
    hits[6] = mockSearchHit("weapons", (float) 7.844109);
    hits[7] = mockSearchHit("other", (float) 7.4610777);
    hits[8] = mockSearchHit("weapons", (float) 7.466502);
    hits[9] = mockSearchHit("weapons", (float) 7.432576);

    Mockito.when(searchHits.getHits()).thenReturn(hits);
    Mockito.when(client.msearch(Mockito.any(), Mockito.eq(RequestOptions.DEFAULT)))
        .thenReturn(responses);

    List<Map<Categories, Double>> expected =
        Lists.list(
            MapBuilder.<Categories, Double>newMapBuilder()
                .put(Categories.WEAPONS, 0.8406778751597274)
                .put(Categories.NUDITY, 0.0)
                .put(Categories.CYBER, 0.0)
                .put(Categories.DRUGS, 0.0)
                .map());

    Assert.assertEquals(expected, elasticHelper.moreLikeThisBulk(indexName, documents));
  }

  private SearchHit mockSearchHit(String target, float score) {
    SearchHit hit = Mockito.mock(SearchHit.class);
    Map<String, Object> sourceAsMap = new HashMap<>();
    sourceAsMap.put("target", target);
    Mockito.when(hit.getSourceAsMap()).thenReturn(sourceAsMap);
    Mockito.when(hit.getScore()).thenReturn(score);
    return hit;
  }

  @Test
  public void checkHealth() throws Exception {
    String indexName = "demo";
    ClusterClient clusterClient = Mockito.mock(ClusterClient.class);
    Mockito.when(client.cluster()).thenReturn(clusterClient);

    Mockito.when(clusterClient.health(Mockito.any(), Mockito.eq(RequestOptions.DEFAULT)))
        .thenReturn(null);
    Assert.assertTrue(elasticHelper.checkHealth(indexName));
  }

  @Test
  public void checkHealthFailure() throws Exception {
    String indexName = "demo";
    ClusterClient clusterClient = Mockito.mock(ClusterClient.class);
    Mockito.when(client.cluster()).thenReturn(clusterClient);

    Mockito.when(clusterClient.health(Mockito.any(), Mockito.eq(RequestOptions.DEFAULT)))
        .thenThrow(ConnectException.class);
    Assert.assertFalse(elasticHelper.checkHealth(indexName));
  }
}
