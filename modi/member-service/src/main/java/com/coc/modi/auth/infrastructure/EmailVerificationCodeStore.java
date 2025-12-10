package com.coc.modi.auth.infrastructure;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class EmailVerificationCodeStore {
	
	private static final String KEY_PREFIX = "email:verify:";
	private final StringRedisTemplate stringRedisTemplate;
	
	// 이메일을 키로 변환 -> 인증 코드를 TTL과 함께 저장
	public void saveCode(String email, String code, Duration ttl) {
		
		stringRedisTemplate.opsForValue().set(buildKey(email), code, ttl);
	}
	
	// Redis에서 이메일에 해당하는 코드를 조회
	public String getCode(String email) {
		
		return stringRedisTemplate.opsForValue().get(buildKey(email));
	}
	
	// 인증 완료 후에 키 삭제 처리
	public void deleteCode(String email) {
		
		stringRedisTemplate.delete(buildKey(email));
	}
	
	// 이메일마다 고유한 키 생성
	private String buildKey(String email) {
		
		return KEY_PREFIX + email;
	}
}
