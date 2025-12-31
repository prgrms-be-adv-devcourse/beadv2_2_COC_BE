package com.coc.modi.seller.chat.domain;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "chat_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
	@JoinColumn(name = "room_id", nullable = false)
	private ChatRoom room;

	@Column(name = "sender_id", nullable = false)
	private Long senderId;

	@Enumerated(EnumType.STRING)
	@Column(name = "sender_role", nullable = false, length = 20)
	private ChatParticipantRole senderRole;

	@Column(name = "content", nullable = false, length = 1000)
	private String content;

	@Column(name = "sent_at", nullable = false)
	private LocalDateTime sentAt;

	@Column(name = "read_at")
	private LocalDateTime readAt;

	@Builder
	private ChatMessage(ChatRoom room,
						Long senderId,
						ChatParticipantRole senderRole,
						String content,
						LocalDateTime sentAt,
						LocalDateTime readAt) {
		this.room = room;
		this.senderId = senderId;
		this.senderRole = senderRole;
		this.content = content;
		this.sentAt = sentAt;
		this.readAt = readAt;
	}

	@PrePersist
	private void onCreate() {
		if (this.sentAt == null) {
			this.sentAt = LocalDateTime.now();
		}
	}
}
