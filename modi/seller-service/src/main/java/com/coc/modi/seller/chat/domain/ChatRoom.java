package com.coc.modi.seller.chat.domain;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chat_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_key", nullable = false, unique = true, length = 64)
	private String roomKey;

	@Builder
	private ChatRoom(String roomKey) {
		this.roomKey = roomKey;
	}

	public static ChatRoom create(String roomKey) {
		return ChatRoom.builder()
				.roomKey(roomKey)
				.build();
	}
}
