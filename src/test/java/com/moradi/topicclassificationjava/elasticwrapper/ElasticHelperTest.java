package com.moradi.topicclassificationjava.elasticwrapper;

import org.apache.commons.csv.CSVRecord;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.client.ClusterClient;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.ConnectException;
import java.util.*;

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
    elasticHelper.createIndex(indexName, fields);
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
    Mockito.when(item.getFailureMessage()).thenReturn("This is a failure message");

    Mockito.when(responses.getResponses()).thenReturn(new MultiSearchResponse.Item[] {item});
    Mockito.when(client.msearch(Mockito.any(), Mockito.eq(RequestOptions.DEFAULT)))
        .thenReturn(responses);

    List<Object> expected = new LinkedList<>();
    Map<String, String> failedMap = new HashMap<>();
    failedMap.put("Failed to evaluate", item.getFailureMessage());
    expected.add(failedMap);

    Assert.assertEquals(expected, elasticHelper.moreLikeThisBulk(indexName, documents));
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
