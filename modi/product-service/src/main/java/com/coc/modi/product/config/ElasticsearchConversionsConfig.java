package com.coc.modi.product.config;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

@Configuration
public class ElasticsearchConversionsConfig {

	@Bean
	public ElasticsearchCustomConversions elasticsearchCustomConversions() {
		return new ElasticsearchCustomConversions(List.of(
				new StringToInstantConverter(),
				new InstantToStringConverter()
		));
	}

	// Accept epoch millis or ISO-8601 strings.
	static class StringToInstantConverter implements Converter<String, Instant> {
		@Override
		public Instant convert(String source) {
			if (source == null || source.isBlank()) {
				return null;
			}
			String value = source.trim();
			if (value.chars().allMatch(Character::isDigit)) {
				long epochMillis = Long.parseLong(value);
				return Instant.ofEpochMilli(epochMillis);
			}
			try {
				return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
			} catch (Exception ignored) {
				return Instant.parse(value);
			}
		}
	}

	static class InstantToStringConverter implements Converter<Instant, String> {
		@Override
		public String convert(Instant source) {
			return source == null ? null : DateTimeFormatter.ISO_INSTANT.format(source);
		}
	}
}
