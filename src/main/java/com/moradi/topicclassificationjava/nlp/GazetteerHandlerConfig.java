package com.moradi.topicclassificationjava.nlp;

import com.moradi.topicclassificationjava.categories.Categories;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.common.collect.MapBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Configuration
public class GazetteerHandlerConfig {
  private static final String WEAPONS_GAZETTEER_FILE = "weapons_gazetteer.txt";
  private static final String NUDITY_GAZETTEER_FILE = "nudity_gazetteer.txt";
  private static final String CYBER_GAZETTEER_FILE = "cyber_gazetteer.txt";
  private static final String DRUGS_GAZETTEER_FILE = "drugs_gazetteer.txt";

  private GazetteerHandler weaponsGazetteer() throws Exception {
    Resource weaponGazetteerResource =
        new ClassPathResource(
            WEAPONS_GAZETTEER_FILE, GazetteerHandlerConfig.class.getClassLoader());
    List<String> words =
        IOUtils.readLines(weaponGazetteerResource.getInputStream(), Charset.defaultCharset());
    return new GazetteerHandler(words);
  }

  private GazetteerHandler nudityGazetteer() throws Exception {
    Resource weaponGazetteerResource =
        new ClassPathResource(NUDITY_GAZETTEER_FILE, GazetteerHandlerConfig.class.getClassLoader());
    List<String> words =
        IOUtils.readLines(weaponGazetteerResource.getInputStream(), Charset.defaultCharset());
    return new GazetteerHandler(words);
  }

  private GazetteerHandler cyberGazetteer() throws Exception {
    Resource weaponGazetteerResource =
        new ClassPathResource(CYBER_GAZETTEER_FILE, GazetteerHandlerConfig.class.getClassLoader());
    List<String> words =
        IOUtils.readLines(weaponGazetteerResource.getInputStream(), Charset.defaultCharset());
    return new GazetteerHandler(words);
  }

  private GazetteerHandler drugsGazetteer() throws Exception {
    Resource weaponGazetteerResource =
        new ClassPathResource(DRUGS_GAZETTEER_FILE, GazetteerHandlerConfig.class.getClassLoader());
    List<String> words =
        IOUtils.readLines(weaponGazetteerResource.getInputStream(), Charset.defaultCharset());
    return new GazetteerHandler(words);
  }

  @Bean
  public Map<Categories, GazetteerHandler> categoriesGazetteerHandlerMap() throws Exception {
    return MapBuilder.<Categories, GazetteerHandler>newMapBuilder()
        .put(Categories.NUDITY, nudityGazetteer())
        .put(Categories.WEAPONS, weaponsGazetteer())
        .put(Categories.CYBER, cyberGazetteer())
        .put(Categories.DRUGS, drugsGazetteer())
        .immutableMap();
  }
}
