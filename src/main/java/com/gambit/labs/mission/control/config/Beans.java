package com.gambit.labs.mission.control.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class Beans {

  @Bean
  public JsonMapper jsonMapper() {
    return JsonMapper.builder()
        .changeDefaultPropertyInclusion(incl ->
            incl.withValueInclusion(JsonInclude.Include.NON_NULL))
        .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
        .build();
  }
}
