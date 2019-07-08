package com.moradi.topicclassificationjava.nlp;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.lucene.analysis.standard.ClassicAnalyzer.STOP_WORDS_SET;
import static org.elasticsearch.search.suggest.completion.context.ContextMapping.FIELD_NAME;

public class GazetteerHandler {
  private final List<String> words;

  public GazetteerHandler(List<String> words) {
    this.words = words;
  }

  public List<Double> classifyByGazetteer(List<String> documents) {
    List<List<String>> documentsTokens =
        documents.stream()
            .map(
                document -> {
                  try {
                    return analyze(document, new StandardAnalyzer(STOP_WORDS_SET));
                  } catch (IOException ignored) {
                    return new ArrayList<String>();
                  }
                })
            .collect(Collectors.toList());
    return documentsTokens.stream().map(this::calcScore).collect(Collectors.toList());
  }

  private double calcScore(List<String> tokens) {
    double count = tokens.stream().filter(this.words::contains).count();
    return Math.max(0, Math.min(0.97, count * count / tokens.size()));
  }

  private List<String> analyze(String text, Analyzer analyzer) throws IOException {
    List<String> result = new ArrayList<>();
    TokenStream tokenStream = analyzer.tokenStream(FIELD_NAME, text);
    CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
    tokenStream.reset();
    while (tokenStream.incrementToken()) {
      result.add(attr.toString());
    }
    return result;
  }
}
