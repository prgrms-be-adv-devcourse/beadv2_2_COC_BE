package com.coc.modi.seller.chat.config;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSubscriptionTracker {

	private final Map<Long, Set<Long>> roomMembers = new ConcurrentHashMap<>();
	private final Map<String, Long> sessionMembers = new ConcurrentHashMap<>();
	private final Map<String, Long> subscriptionRooms = new ConcurrentHashMap<>();
	private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();

	public void registerSession(String sessionId, Long memberId) {
		if (sessionId == null || memberId == null) {
			return;
		}
		sessionMembers.put(sessionId, memberId);
	}

	public void registerSubscription(String sessionId, String subscriptionId, Long roomId, Long memberId) {
		if (subscriptionId == null || roomId == null || memberId == null) {
			return;
		}
		subscriptionRooms.put(subscriptionId, roomId);
		roomMembers.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet()).add(memberId);

		if (sessionId != null) {
			sessionSubscriptions.computeIfAbsent(sessionId, key -> ConcurrentHashMap.newKeySet()).add(subscriptionId);
		}
	}

	public void unregisterSubscription(String sessionId, String subscriptionId) {
		if (subscriptionId == null) {
			return;
		}
		Long roomId = subscriptionRooms.remove(subscriptionId);
		Long memberId = sessionMembers.get(sessionId);
		if (roomId != null && memberId != null) {
			Set<Long> members = roomMembers.get(roomId);
			if (members != null) {
				members.remove(memberId);
				if (members.isEmpty()) {
					roomMembers.remove(roomId);
				}
			}
		}

		if (sessionId != null) {
			Set<String> subs = sessionSubscriptions.get(sessionId);
			if (subs != null) {
				subs.remove(subscriptionId);
				if (subs.isEmpty()) {
					sessionSubscriptions.remove(sessionId);
				}
			}
		}
	}

	public void unregisterSession(String sessionId) {
		if (sessionId == null) {
			return;
		}
		Long memberId = sessionMembers.remove(sessionId);
		Set<String> subs = sessionSubscriptions.remove(sessionId);
		if (memberId == null || subs == null) {
			return;
		}
		for (String subscriptionId : subs) {
			Long roomId = subscriptionRooms.remove(subscriptionId);
			if (roomId == null) {
				continue;
			}
			Set<Long> members = roomMembers.get(roomId);
			if (members != null) {
				members.remove(memberId);
				if (members.isEmpty()) {
					roomMembers.remove(roomId);
				}
			}
		}
	}

	public Set<Long> getActiveMembers(Long roomId) {
		if (roomId == null) {
			return Collections.emptySet();
		}
		return roomMembers.getOrDefault(roomId, Collections.emptySet());
	}
}
