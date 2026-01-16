package com.coc.modi.member.auth.infrastructure;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class EmailVerificationTokenStore {

    private static final String KEY_PREFIX = "email:verify:token:";
    private final StringRedisTemplate stringRedisTemplate;

    public void saveToken(String email, String token, Duration ttl) {

        stringRedisTemplate.opsForValue().set(buildKey(token), email, ttl);
    }

    public String getEmail(String token) {

        return stringRedisTemplate.opsForValue().get(buildKey(token));
    }

    public void deleteToken(String token) {

        stringRedisTemplate.delete(buildKey(token));
    }

    private String buildKey(String token) {

        return KEY_PREFIX + token;
    }
}
