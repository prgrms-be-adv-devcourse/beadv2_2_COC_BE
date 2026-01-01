package com.coc.modi.seller.chat.config;

import java.util.Set;

public interface ChatSubscriptionTracker {

	void registerSession(String sessionId, Long memberId);

	void registerSubscription(String sessionId, String subscriptionId, Long roomId, Long memberId);

	void unregisterSubscription(String sessionId, String subscriptionId);

	void unregisterSession(String sessionId);

	Set<Long> getActiveMembers(Long roomId);
}
