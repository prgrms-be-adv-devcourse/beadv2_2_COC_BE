package com.coc.modi.review.cache;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.coc.modi.review.config.ReviewPolicyProperties;

@Component
public class ReturnedRentalCache {

	private static final String KEY_PREFIX = "review:returned-rental-item:";

	private final RedisTemplate<String, ReturnedRentalItem> returnedRentalRedisTemplate;
	private final Duration ttl;

	public ReturnedRentalCache(
			RedisTemplate<String, ReturnedRentalItem> returnedRentalRedisTemplate,
			ReviewPolicyProperties reviewPolicyProperties
	) {
		this.returnedRentalRedisTemplate = returnedRentalRedisTemplate;
		this.ttl = reviewPolicyProperties.reviewableWindow();
	}

	public Optional<ReturnedRentalItem> find(Long rentalItemId) {
		return Optional.ofNullable(returnedRentalRedisTemplate.opsForValue().get(key(rentalItemId)));
	}

	public void save(ReturnedRentalItem item) {
		returnedRentalRedisTemplate.opsForValue().set(key(item.rentalItemId()), item, ttl);
	}

	private String key(Long rentalItemId) {
		return KEY_PREFIX + rentalItemId;
	}
}
