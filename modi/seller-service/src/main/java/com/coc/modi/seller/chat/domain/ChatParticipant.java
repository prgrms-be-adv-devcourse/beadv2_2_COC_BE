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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
		name = "chat_participant",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_chat_participant_room_member",
						columnNames = {"room_id", "member_id"}
				)
		}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatParticipant extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
	@JoinColumn(name = "room_id", nullable = false)
	private ChatRoom room;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	private ChatParticipantRole role;

	@Column(name = "last_read_message_id")
	private Long lastReadMessageId;

	@Column(name = "last_read_at")
	private LocalDateTime lastReadAt;

	@Column(name = "left_at")
	private LocalDateTime leftAt;

	@Builder
	private ChatParticipant(ChatRoom room,
							Long memberId,
							ChatParticipantRole role,
							Long lastReadMessageId,
							LocalDateTime lastReadAt,
							LocalDateTime leftAt) {
		this.room = room;
		this.memberId = memberId;
		this.role = role;
		this.lastReadMessageId = lastReadMessageId;
		this.lastReadAt = lastReadAt;
		this.leftAt = leftAt;
	}

	public void markRead(Long messageId, LocalDateTime readAt) {
		if (messageId == null) {
			return;
		}
		if (this.lastReadMessageId == null || messageId > this.lastReadMessageId) {
			this.lastReadMessageId = messageId;
			this.lastReadAt = readAt;
		}
	}

	public boolean isActive() {
		return this.leftAt == null;
	}

	public void leave(LocalDateTime leftAt) {
		if (this.leftAt != null) {
			return;
		}
		this.leftAt = leftAt != null ? leftAt : LocalDateTime.now();
	}

	public void reactivate() {
		if (this.leftAt == null) {
			return;
		}
		this.leftAt = null;
	}
}
