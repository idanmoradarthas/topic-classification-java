package com.moradi.topicclassificationjava.nlp;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.apache.lucene.analysis.standard.ClassicAnalyzer.STOP_WORDS_SET;
import static org.elasticsearch.search.suggest.completion.context.ContextMapping.FIELD_NAME;

public class GazetteerHandler {
  private final Pattern pattern;

  public GazetteerHandler(List<String> words) {
    String regex =
        words.stream()
            .map(word -> String.format("(%s)", word))
            .collect(Collectors.joining("|"))
            .concat("(\\s|$|\\.|,|!|;|:|\\?)");
    this.pattern = Pattern.compile(regex, CASE_INSENSITIVE);
  }

  public List<Double> classifyByGazetteer(List<String> documents) {
    return documents.stream()
        .map(
            document -> {
              double count = 0;
              try {
                count = countTokens(document, new StandardAnalyzer(STOP_WORDS_SET));
              } catch (IOException ignored) {

              }
              Matcher matcher = this.pattern.matcher(document);
              long matchesSum =
                  Stream.iterate(0, i -> i + 1).filter(i -> !matcher.find()).findFirst().get();
              return (count > 0)
                  ? Math.max(0, Math.min(0.97, matchesSum * matchesSum / count))
                  : 0.0;
            })
        .collect(Collectors.toList());
  }

  private int countTokens(String text, Analyzer analyzer) throws IOException {
    List<String> result = new ArrayList<>();
    TokenStream tokenStream = analyzer.tokenStream(FIELD_NAME, text);
    CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
    tokenStream.reset();
    while (tokenStream.incrementToken()) {
      result.add(attr.toString());
    }
    return result.size();
  }
}
