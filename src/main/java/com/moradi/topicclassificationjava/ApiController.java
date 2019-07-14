package com.moradi.topicclassificationjava;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moradi.topicclassificationjava.categories.Categories;
import com.moradi.topicclassificationjava.elasticwrapper.ElasticHelper;
import com.moradi.topicclassificationjava.nlp.GazetteerClassifier;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RestController
@Import(ElasticHelper.class)
public class ApiController {

  private static final String DATASET_FILE = "full_set.csv";
  private static final String DOCUMENT_DEFINITION_FILE =
      "elasticsearch configuration files/document_definition.json";
  private static final String SETTINGS_DEFINITION_FILE =
      "elasticsearch configuration files/settings_definition.json";
  private static final String COUNTRIES_NAMES_FILE =
      "elasticsearch configuration files/counties_names.txt";
  private static final String FORTUNE_500_FILE =
      "elasticsearch configuration files/Fortune 500.txt";

  @Value("${elasticsearch.indexName}")
  private String indexName;

  @Value("${elasticsearch.server.millisToSleep}")
  private int millisToSleep;

  private final ElasticHelper elasticHelper;
  private final GazetteerClassifier gazetteerClassifier;

  @Autowired
  public ApiController(ElasticHelper elasticHelper, GazetteerClassifier gazetteerClassifier) {
    this.elasticHelper = elasticHelper;
    this.gazetteerClassifier = gazetteerClassifier;
  }

  private static Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

  @PostConstruct
  private void postConstruct() throws IOException, InterruptedException {
    if (!this.elasticHelper.checkHealth(this.indexName)) {
      LOGGER.info("sleep for a minute");
      Thread.sleep(this.millisToSleep);
    }

    if (this.elasticHelper.indexExists(this.indexName)) {
      LOGGER.info("Index pre-exists");
      this.elasticHelper.deleteIndex(this.indexName);
      LOGGER.info("Index deleted");
    }
    this.elasticHelper.createIndex(
        this.indexName, getDocumentDefinition(), getSettingsDefinition());
    LOGGER.info(String.format("New Index Created: %s", this.indexName));

    List<CSVRecord> records = LoadDataSet();
    LOGGER.info("Index documents");
    this.elasticHelper.bulkIndexDocuments(this.indexName, records);
  }

  private Map<String, Object> getDocumentDefinition() throws IOException {
    LOGGER.info("Read document definition file");
    Resource documentDefinitionResource =
        new ClassPathResource(DOCUMENT_DEFINITION_FILE, ApiController.class.getClassLoader());
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
    return mapper.readValue(
        IOUtils.toString(documentDefinitionResource.getInputStream(), Charset.defaultCharset()),
        new TypeReference<Map<String, Object>>() {});
  }

  private Map<String, Object> getSettingsDefinition() throws IOException {
    LOGGER.info("Read settings definition file");
    Resource settingsDefinitionResource =
        new ClassPathResource(SETTINGS_DEFINITION_FILE, ApiController.class.getClassLoader());
    Resource countiesNamesResource =
        new ClassPathResource(COUNTRIES_NAMES_FILE, ApiController.class.getClassLoader());
    Resource fortuneResource =
        new ClassPathResource(FORTUNE_500_FILE, ApiController.class.getClassLoader());
    String settings =
        IOUtils.toString(settingsDefinitionResource.getInputStream(), Charset.defaultCharset());
    List<String> countiesNames =
        IOUtils.readLines(countiesNamesResource.getInputStream(), Charset.defaultCharset());
    List<String> fortune =
        IOUtils.readLines(fortuneResource.getInputStream(), Charset.defaultCharset());
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
    Map<String, Object> settingsMap =
        mapper.readValue(settings, new TypeReference<Map<String, Object>>() {});
    Map<String, Map<String, Map<String, Object>>> analysis =
        (Map<String, Map<String, Map<String, Object>>>) settingsMap.get("analysis");
    analysis.get("filter").get("countries_names").put("stopwords", countiesNames);
    analysis.get("filter").get("fortune_500").put("stopwords", fortune);
    return settingsMap;
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
  public List<List<Map<String, Object>>> getClassification(@RequestBody String request)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
    List<Map<String, String>> documentsList =
        mapper.readValue(request, new TypeReference<List<Map<String, String>>>() {});
    List<String> documents =
        documentsList.stream().map(docMap -> docMap.get("text")).collect(Collectors.toList());
    List<Map<Categories, Double>> elasticResponses =
        this.elasticHelper.moreLikeThisBulk(this.indexName, documents);
    List<Map<Categories, Double>> gazetteerResponses =
        this.gazetteerClassifier.classifyByGazetteers(documents);
    List<List<Map<String, Object>>> response =
        calcResponse(documents, elasticResponses, gazetteerResponses);
    LOGGER.info(String.format("Response: %s", response));
    return response;
  }

  private List<List<Map<String, Object>>> calcResponse(
      List<String> documents,
      List<Map<Categories, Double>> elasticResponses,
      List<Map<Categories, Double>> gazetteerResponses) {
    return IntStream.range(0, documents.size())
        .boxed()
        .map(
            i -> {
              Map<Categories, Double> elasticResponse = elasticResponses.get(i);
              Map<Categories, Double> gazetteerResponse = gazetteerResponses.get(i);
              return Stream.of(elasticResponse, gazetteerResponse)
                  .flatMap(map -> map.entrySet().stream())
                  .collect(
                      Collectors.toMap(
                          Map.Entry::getKey, Map.Entry::getValue, probabilityCombiner()));
            })
        .map(
            map ->
                map.entrySet().stream()
                    .map(
                        entry -> {
                          Map<String, Object> smallMap = new HashMap<>();
                          smallMap.put("topic", entry.getKey().getText());
                          smallMap.put("match_prob", entry.getValue());
                          return smallMap;
                        })
                    .collect(Collectors.toList()))
        .collect(Collectors.toList());
  }

  private BinaryOperator<Double> probabilityCombiner() {
    return (v1, v2) -> {
      if ((v1 > 0) && (v2 > 0)) {
        return 0.7 * v1 + 0.3 * v2;
      } else if (v1 > 0) {
        return v1;
      } else if (v2 > 0) {
        return v2;
      }
      return 0.0;
    };
  }
}
