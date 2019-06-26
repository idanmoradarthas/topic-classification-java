package com.moradi.topicclassificationjava;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingFilterConfig {
  @Bean
  public CommonsRequestLoggingFilter logFilter() {
    CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
    filter.setIncludeClientInfo(true);
    filter.setIncludePayload(true);
    filter.setIncludeHeaders(true);
    filter.setMaxPayloadLength(1000000);
    return filter;
  }
}
