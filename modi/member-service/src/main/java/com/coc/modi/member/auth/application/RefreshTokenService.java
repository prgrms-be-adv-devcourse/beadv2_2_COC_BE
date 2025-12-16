package com.coc.modi.member.auth.application;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;



import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	
	private final StringRedisTemplate redisTemplate;
	
	private static final String PREFIX = "refresh:";
	
	public void save(Long memberId, String refreshToken, Duration ttl) {
		
		redisTemplate.opsForValue().set(PREFIX + memberId, refreshToken, ttl);
	}
	
	public String find(Long memberId) {
		
		return redisTemplate.opsForValue().get(PREFIX + memberId);
	}
	
	public void delete(Long memberId) {
		
		redisTemplate.delete(PREFIX + memberId);
	}
	
	public boolean matches(Long memberId, String refreshToken) {
		
		String saved = find(memberId);
		return saved != null && saved.equals(refreshToken);
	}
}
