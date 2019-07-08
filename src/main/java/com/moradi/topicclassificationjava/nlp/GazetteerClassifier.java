package com.moradi.topicclassificationjava.nlp;

import com.moradi.topicclassificationjava.categories.Categories;
import org.elasticsearch.common.collect.MapBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Import({GazetteerHandlerConfig.class})
@Component
public class GazetteerClassifier {

  private final Map<Categories, GazetteerHandler> categoriesGazetteerHandlerMap;

  public GazetteerClassifier(
      @Autowired Map<Categories, GazetteerHandler> categoriesGazetteerHandlerMap) {
    this.categoriesGazetteerHandlerMap = categoriesGazetteerHandlerMap;
  }

  public List<Map<Categories, Double>> classifyByGazetteers(List<String> documents) {
    List<List<Map<Categories, Double>>> categoriesProbList =
        this.categoriesGazetteerHandlerMap.entrySet().stream()
            .map(
                entry ->
                    entry.getValue().classifyByGazetteer(documents).stream()
                        .map(
                            prob ->
                                MapBuilder.<Categories, Double>newMapBuilder()
                                    .put(entry.getKey(), prob)
                                    .map())
                        .collect(Collectors.toList()))
            .collect(Collectors.toList());
    return IntStream.range(0, documents.size())
        .boxed()
        .map(
            i -> {
              MapBuilder<Categories, Double> builder = MapBuilder.newMapBuilder();
              categoriesProbList.stream().map(list -> list.get(i)).forEach(builder::putAll);
              return builder.map();
            })
        .collect(Collectors.toList());
  }
}
