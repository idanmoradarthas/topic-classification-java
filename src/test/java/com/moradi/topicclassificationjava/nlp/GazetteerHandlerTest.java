package com.moradi.topicclassificationjava.nlp;

import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class GazetteerHandlerTest {

  @Test
  public void classifyByGazetteer() {
      String sampleText = "This is a sample Lucene Analyzers test";
      List<String> words = Lists.list("lucene", "test");
      GazetteerHandler gazetteerHandler = new GazetteerHandler(words);
      List<Double> result = gazetteerHandler.classifyByGazetteer(Lists.list(sampleText));
      assertEquals(Lists.list(0.97), result);
  }
}