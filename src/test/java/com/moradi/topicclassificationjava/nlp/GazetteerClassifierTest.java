package com.moradi.topicclassificationjava.nlp;

import com.moradi.topicclassificationjava.categories.Categories;
import org.assertj.core.util.Lists;
import org.elasticsearch.common.collect.MapBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class GazetteerClassifierTest {

  @Mock private GazetteerHandler weaponsGazetteer;

  @Mock private GazetteerHandler nudityGazetteer;

  private GazetteerClassifier gazetteerClassifier;

  @Before
  public void setUp() {
    this.gazetteerClassifier =
        new GazetteerClassifier(
            MapBuilder.<Categories, GazetteerHandler>newMapBuilder()
                .put(Categories.NUDITY, this.nudityGazetteer)
                .put(Categories.WEAPONS, this.weaponsGazetteer)
                .immutableMap());
  }

  @Test
  public void classifyByGazetteers() {
    String sampleText = "This is a sample Lucene Analyzers test";
    List<String> documents = Lists.list(sampleText);

    Mockito.when(this.nudityGazetteer.classifyByGazetteer(documents)).thenReturn(Lists.list(0.4));
    Mockito.when(this.weaponsGazetteer.classifyByGazetteer(documents)).thenReturn(Lists.list(0.6));

    List<Map<Categories, Double>> expected =
        Lists.list(
            MapBuilder.<Categories, Double>newMapBuilder()
                .put(Categories.NUDITY, 0.4)
                .put(Categories.WEAPONS, 0.6)
                .map());
    List<Map<Categories, Double>> result = this.gazetteerClassifier.classifyByGazetteers(documents);
    Assert.assertEquals(expected, result);
  }
}
