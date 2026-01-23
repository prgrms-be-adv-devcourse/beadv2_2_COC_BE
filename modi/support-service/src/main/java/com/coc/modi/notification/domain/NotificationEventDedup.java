package com.coc.modi.notification.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
		name = "notification_event_dedup",
		schema = "support",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_notification_event_dedup_event_consumer",
						columnNames = {"event_id", "consumer"}
				)
		}
)
public class NotificationEventDedup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_id", nullable = false, length = 36)
	private String eventId;

	@Column(name = "consumer", nullable = false, length = 50)
	private String consumer;

	@Column(name = "processed_at", nullable = false)
	private LocalDateTime processedAt;

	private NotificationEventDedup(String eventId, String consumer, LocalDateTime processedAt) {

		this.eventId = eventId;
		this.consumer = consumer;
		this.processedAt = processedAt;
	}

	public static NotificationEventDedup create(String eventId, String consumer) {

		return new NotificationEventDedup(eventId, consumer, LocalDateTime.now());
	}
}
