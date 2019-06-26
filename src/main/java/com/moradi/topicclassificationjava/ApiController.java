package com.moradi.topicclassificationjava;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moradi.topicclassificationjava.elasticwrapper.ElasticHelper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Import(ElasticHelper.class)
public class ApiController {

  private static final String DATASET_FILE = "full_set.csv";

  @Value("${elasticsearch.indexName}")
  private String indexName;

  private final ElasticHelper elasticHelper;

  public ApiController(@Autowired ElasticHelper elasticHelper) {
    this.elasticHelper = elasticHelper;
  }

  private static Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

  @PostConstruct
  private void postConstruct() throws IOException, InterruptedException {
    if (!elasticHelper.checkHealth(indexName)) {
      LOGGER.info("sleep for a minute");
      Thread.sleep(40000);
    }

    if (elasticHelper.indexExists(indexName)) {
      LOGGER.info("Index pre-exists");
      elasticHelper.deleteIndex(indexName);
      LOGGER.info("Index deleted");
    }
    elasticHelper.createIndex(indexName, getDocumentDefinition());
    LOGGER.info(String.format("New Index Created: %s", indexName));

    List<CSVRecord> records = LoadDataSet();
    LOGGER.info("Index documents");
    elasticHelper.bulkIndexDocuments(indexName, records);
  }

  private Map<String, Object> getDocumentDefinition() {
    Map<String, Object> doc = new HashMap<>();
    doc.put("type", "text");
    Map<String, Object> target = new HashMap<>();
    target.put("type", "keyword");
    Map<String, Object> documentDefinition = new HashMap<>();
    documentDefinition.put("doc", doc);
    documentDefinition.put("target", target);
    return documentDefinition;
  }

  private List<CSVRecord> LoadDataSet() throws IOException {
    LOGGER.info("Read CSV file");
    Resource datasetResource =
            new ClassPathResource(DATASET_FILE, ApiController.class.getClassLoader());
    Reader reader = new InputStreamReader(datasetResource.getInputStream());
    CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
    List<CSVRecord> records = csvParser.getRecords();
    csvParser.close();
    return records;
  }

  @PostMapping(value = "/classify/v1.0", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Object> getClassification(@RequestBody String request) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
    List<Map<String, String>> documentsList =
        mapper.readValue(request, new TypeReference<List<Map<String, String>>>() {});
    List<String> documents =
        documentsList.stream().map(docMap -> docMap.get("text")).collect(Collectors.toList());
    List<Object> response = elasticHelper.moreLikeThisBulk(indexName, documents);
    LOGGER.info(String.format("Response: %s", response));
    return response;
  }
}
