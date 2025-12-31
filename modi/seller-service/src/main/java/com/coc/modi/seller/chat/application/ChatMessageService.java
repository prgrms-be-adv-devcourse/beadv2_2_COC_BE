package com.coc.modi.seller.chat.application;

import com.coc.modi.seller.chat.application.dto.ChatMessageResponse;
import com.coc.modi.seller.chat.application.dto.ChatMessageSendCommand;
import com.coc.modi.seller.chat.domain.ChatMessage;
import com.coc.modi.seller.chat.domain.ChatMessageRepository;
import com.coc.modi.seller.chat.domain.ChatParticipantRole;
import com.coc.modi.seller.chat.domain.ChatRoom;
import com.coc.modi.seller.chat.domain.ChatRoomRepository;
import com.coc.modi.seller.chat.exception.ChatInputInvalidException;
import com.coc.modi.seller.chat.exception.ChatRoomNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;

	@Transactional
	public ChatMessageResponse sendMessage(ChatMessageSendCommand command) {
		if (command == null || command.roomId() == null || command.senderId() == null) {
			throw new ChatInputInvalidException("메시지 전송 정보가 올바르지 않습니다.");
		}
		if (command.content() == null || command.content().isBlank()) {
			throw new ChatInputInvalidException("메시지 내용이 비어 있습니다.");
		}

		ChatRoom room = chatRoomRepository.findById(command.roomId())
				.orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다. roomId=" + command.roomId()));

		ChatParticipantRole role = resolveRole(command.senderRole());

		ChatMessage message = ChatMessage.builder()
				.room(room)
				.senderId(command.senderId())
				.senderRole(role)
				.content(command.content())
				.build();

		ChatMessage saved = chatMessageRepository.save(message);
		return ChatMessageResponse.from(saved);
	}

	private ChatParticipantRole resolveRole(String role) {
		if (role == null || role.isBlank()) {
			throw new ChatInputInvalidException("요청자 역할이 비어 있습니다.");
		}
		try {
			return ChatParticipantRole.valueOf(role.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new ChatInputInvalidException("지원하지 않는 역할입니다. role=" + role);
		}
	}
}
