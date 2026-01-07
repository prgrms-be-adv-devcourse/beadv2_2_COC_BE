package com.coc.modi.seller.notification;

import com.coc.modi.common.BaseEntity;
import com.coc.modi.kafka.event.NotificationEvent;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
		name = "notification_outbox",
		indexes = {
				@Index(name = "idx_notification_outbox_status_next", columnList = "status,next_attempt_at")
		}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationOutbox extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_id", nullable = false, length = 36)
	private String eventId;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Column(name = "receiver_id", nullable = false)
	private Long receiverId;

	@Column(name = "type", nullable = false, length = 50)
	private String type;

	@Column(name = "title", nullable = false, length = 100)
	private String title;

	@Column(name = "content", nullable = false, length = 255)
	private String content;

	@Column(name = "reference_type", length = 50)
	private String referenceType;

	@Column(name = "reference_id", length = 50)
	private String referenceId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private NotificationOutboxStatus status;

	@Column(name = "retry_count", nullable = false)
	private int retryCount;

	@Column(name = "next_attempt_at")
	private Instant nextAttemptAt;

	@Column(name = "last_error", length = 1000)
	private String lastError;

	@Builder
	private NotificationOutbox(
			String eventId,
			Instant occurredAt,
			Long receiverId,
			String type,
			String title,
			String content,
			String referenceType,
			String referenceId,
			NotificationOutboxStatus status,
			int retryCount,
			Instant nextAttemptAt,
			String lastError
	) {

		this.eventId = eventId;
		this.occurredAt = occurredAt;
		this.receiverId = receiverId;
		this.type = type;
		this.title = title;
		this.content = content;
		this.referenceType = referenceType;
		this.referenceId = referenceId;
		this.status = status != null ? status : NotificationOutboxStatus.PENDING;
		this.retryCount = Math.max(retryCount, 0);
		this.nextAttemptAt = nextAttemptAt;
		this.lastError = lastError;
	}

	public static NotificationOutbox from(NotificationEvent event) {

		return NotificationOutbox.builder()
				.eventId(event.eventId())
				.occurredAt(event.occurredAt())
				.receiverId(event.receiverId())
				.type(event.type())
				.title(event.title())
				.content(event.content())
				.referenceType(event.referenceType())
				.referenceId(event.referenceId())
				.status(NotificationOutboxStatus.PENDING)
				.retryCount(0)
				.build();
	}
}
