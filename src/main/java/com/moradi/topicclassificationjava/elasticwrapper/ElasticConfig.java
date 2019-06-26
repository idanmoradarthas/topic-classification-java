package com.moradi.topicclassificationjava.elasticwrapper;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class ElasticConfig {
  @Value("${elasticsearch.host}")
  public String host;

  @Value("${elasticsearch.port}")
  public int port;

  @Bean
  public RestHighLevelClient client() throws UnknownHostException {
    return new RestHighLevelClient(
        RestClient.builder(new HttpHost(InetAddress.getByName(this.host), this.port)));
  }
}
