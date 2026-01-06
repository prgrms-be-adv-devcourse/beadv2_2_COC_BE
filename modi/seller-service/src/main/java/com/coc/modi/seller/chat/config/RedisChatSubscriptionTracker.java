package com.coc.modi.seller.chat.config;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisChatSubscriptionTracker implements ChatSubscriptionTracker {

	private static final Duration SESSION_TTL = Duration.ofHours(2);

	private static final String SESSION_KEY_PREFIX = "chat:ws:session:";
	private static final String SESSION_SUBS_SUFFIX = ":subs";
	private static final String SUB_KEY_PREFIX = "chat:ws:sub:";
	private static final String ROOM_MEMBERS_PREFIX = "chat:ws:room:";
	private static final String ROOM_MEMBERS_SUFFIX = ":members";

	private final StringRedisTemplate stringRedisTemplate;

	@Override
	public void registerSession(String sessionId, Long memberId) {
		if (sessionId == null || memberId == null) {
			return;
		}
		stringRedisTemplate.opsForValue().set(sessionKey(sessionId), memberId.toString(), SESSION_TTL);
		stringRedisTemplate.expire(sessionSubsKey(sessionId), SESSION_TTL);
	}

	@Override
	public void registerSubscription(String sessionId, String subscriptionId, Long roomId, Long memberId) {
		if (subscriptionId == null || roomId == null || memberId == null) {
			return;
		}
		stringRedisTemplate.opsForValue().set(subKey(subscriptionId), roomId.toString(), SESSION_TTL);
		stringRedisTemplate.opsForSet().add(roomMembersKey(roomId), memberId.toString());
		stringRedisTemplate.expire(roomMembersKey(roomId), SESSION_TTL);

		if (sessionId != null) {
			stringRedisTemplate.opsForSet().add(sessionSubsKey(sessionId), subscriptionId);
			stringRedisTemplate.expire(sessionSubsKey(sessionId), SESSION_TTL);
		}
	}

	@Override
	public void unregisterSubscription(String sessionId, String subscriptionId) {
		if (subscriptionId == null) {
			return;
		}
		String roomIdValue = stringRedisTemplate.opsForValue().get(subKey(subscriptionId));
		stringRedisTemplate.delete(subKey(subscriptionId));

		String memberIdValue = sessionId != null ? stringRedisTemplate.opsForValue().get(sessionKey(sessionId)) : null;
		if (roomIdValue != null && memberIdValue != null) {
			stringRedisTemplate.opsForSet().remove(roomMembersKey(roomIdValue), memberIdValue);
		}

		if (sessionId != null) {
			stringRedisTemplate.opsForSet().remove(sessionSubsKey(sessionId), subscriptionId);
		}
	}

	@Override
	public void unregisterSession(String sessionId) {
		if (sessionId == null) {
			return;
		}
		Set<String> subs = stringRedisTemplate.opsForSet().members(sessionSubsKey(sessionId));
		stringRedisTemplate.delete(sessionSubsKey(sessionId));
		String memberIdValue = stringRedisTemplate.opsForValue().get(sessionKey(sessionId));
		stringRedisTemplate.delete(sessionKey(sessionId));
		if (subs == null || subs.isEmpty() || memberIdValue == null) {
			return;
		}
		for (String subscriptionId : subs) {
			String roomIdValue = stringRedisTemplate.opsForValue().get(subKey(subscriptionId));
			stringRedisTemplate.delete(subKey(subscriptionId));
			if (roomIdValue != null) {
				stringRedisTemplate.opsForSet().remove(roomMembersKey(roomIdValue), memberIdValue);
			}
		}
	}

	@Override
	public Set<Long> getActiveMembers(Long roomId) {
		if (roomId == null) {
			return Set.of();
		}
		Set<String> members = stringRedisTemplate.opsForSet().members(roomMembersKey(roomId));
		if (members == null || members.isEmpty()) {
			return Set.of();
		}
		return members.stream()
				.map(this::parseLong)
				.filter(value -> value != null)
				.collect(Collectors.toSet());
	}

	private String sessionKey(String sessionId) {
		return SESSION_KEY_PREFIX + sessionId;
	}

	private String sessionSubsKey(String sessionId) {
		return SESSION_KEY_PREFIX + sessionId + SESSION_SUBS_SUFFIX;
	}

	private String subKey(String subscriptionId) {
		return SUB_KEY_PREFIX + subscriptionId;
	}

	private String roomMembersKey(Long roomId) {
		return ROOM_MEMBERS_PREFIX + roomId + ROOM_MEMBERS_SUFFIX;
	}

	private String roomMembersKey(String roomId) {
		return ROOM_MEMBERS_PREFIX + roomId + ROOM_MEMBERS_SUFFIX;
	}

	private Long parseLong(String value) {
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException ex) {
			return null;
		}
	}
}
