package com.coc.modi.member.auth.infrastructure;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class PasswordResetCodeStore {
	
	private static final String KEY_PREFIX = "password:reset:";
	private final StringRedisTemplate stringRedisTemplate;
	
	public void saveCode(String email, String code, Duration ttl) {
		
		stringRedisTemplate.opsForValue().set(buildKey(email), code, ttl);
	}
	
	public String getCode(String email) {
		
		return stringRedisTemplate.opsForValue().get(buildKey(email));
	}
	
	public void deleteCode(String email) {
		
		stringRedisTemplate.delete(buildKey(email));
	}
	
	private String buildKey(String email) {
		
		return KEY_PREFIX + email;
	}
}
