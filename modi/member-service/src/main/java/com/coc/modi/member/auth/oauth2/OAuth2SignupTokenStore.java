package com.coc.modi.member.auth.oauth2;

import java.time.Duration;
import java.util.UUID;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.member.auth.config.OAuth2Properties;
import com.coc.modi.member.member.exception.MemberException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2SignupTokenStore {

	private static final String PREFIX = "oauth2:signup:";

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final OAuth2Properties properties;

	public String issue(OAuth2SignupPayload payload) {

		String token = UUID.randomUUID().toString().replace("-", "");
		String key = PREFIX + token;
		String json = writePayload(payload);
		redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(properties.getSignupTokenTtlMinutes()));
		return token;
	}

	public OAuth2SignupPayload getPayload(String token) {

		String value = redisTemplate.opsForValue().get(PREFIX + token);
		if (value == null) {
			throw new MemberException(ErrorCode.INVALID_INPUT, "OAuth2 가입 토큰이 유효하지 않습니다.");
		}
		return readPayload(value);
	}

	public void delete(String token) {

		redisTemplate.delete(PREFIX + token);
	}

	private String writePayload(OAuth2SignupPayload payload) {

		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to serialize OAuth2 signup payload", ex);
		}
	}

	private OAuth2SignupPayload readPayload(String value) {

		try {
			return objectMapper.readValue(value, OAuth2SignupPayload.class);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to deserialize OAuth2 signup payload", ex);
		}
	}
}
