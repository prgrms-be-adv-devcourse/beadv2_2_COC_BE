package com.coc.modi.product.search.infrastructure;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component("elasticsearch")
public class ElasticsearchHealth implements HealthIndicator {
	
	private final ElasticsearchStatus elasticsearchStatus;
	
	@Override
	public Health health() {
		
		ElasticsearchStatus.State state = elasticsearchStatus.getState();
		
		return switch (state) {
			case UNKNOWN -> Health.unknown().build();
			case AVAILABLE ->  Health.up().build();
			case UNAVAILABLE ->  {
				Health.Builder builder = Health.down();
				elasticsearchStatus.getLastError()
						.ifPresent(e -> builder.withDetail("error", e.getMessage()));
				yield builder.build();
			}
		};
	}
}
