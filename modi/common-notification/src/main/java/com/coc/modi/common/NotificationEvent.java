package com.coc.modi.common;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * @deprecated Prefer com.coc.modi.kafka.event.NotificationEvent for Kafka payloads.
 */
@Deprecated(since = "2026-01-20", forRemoval = false)
@Getter
@Setter
public class NotificationEvent {
	
	private NotificationType type;
	private Long receiverId;
	private String title;
	private String content;
	
	private String referenceType;
	private Long referenceId;
	
	private Set<NotificationChannel> channels;
	
	private Map<String, Object> metadata;
}
