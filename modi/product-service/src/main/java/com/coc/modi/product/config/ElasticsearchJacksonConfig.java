package com.coc.modi.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.elastic.clients.json.jackson.JacksonJsonpMapper;

@Configuration
public class ElasticsearchJacksonConfig {
	
	@Bean
	public JacksonJsonpMapper elasticsearchJsonpMapper() {
		ObjectMapper mapper = new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		
		return new JacksonJsonpMapper(mapper);
	}
}
