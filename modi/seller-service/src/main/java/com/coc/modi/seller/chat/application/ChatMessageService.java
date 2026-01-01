package com.coc.modi.seller.chat.application;

import com.coc.modi.seller.chat.application.dto.ChatMessageResponse;
import com.coc.modi.seller.chat.application.dto.ChatMessageSendCommand;
import com.coc.modi.seller.chat.application.dto.ChatMessageSliceResponse;
import com.coc.modi.seller.chat.domain.ChatMessage;
import com.coc.modi.seller.chat.domain.ChatMessageRepository;
import com.coc.modi.seller.chat.domain.ChatParticipant;
import com.coc.modi.seller.chat.domain.ChatParticipantRepository;
import com.coc.modi.seller.chat.domain.ChatParticipantRole;
import com.coc.modi.seller.chat.domain.ChatRoom;
import com.coc.modi.seller.chat.domain.ChatRoomRepository;
import com.coc.modi.seller.chat.exception.ChatInputInvalidException;
import com.coc.modi.seller.chat.exception.ChatRoomNotFoundException;
import com.coc.modi.seller.chat.exception.ChatAccessDeniedException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final ChatParticipantRepository chatParticipantRepository;

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

		chatParticipantRepository.findByRoomIdAndMemberId(room.getId(), command.senderId())
				.orElseThrow(() -> new ChatAccessDeniedException("채팅방 참가자가 아닙니다. roomId=" + room.getId()));

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

	@Transactional
	public void markReadForMembers(Long roomId, Collection<Long> memberIds, Long messageId) {
		if (roomId == null || messageId == null || memberIds == null || memberIds.isEmpty()) {
			return;
		}
		LocalDateTime readAt = LocalDateTime.now();
		for (Long memberId : memberIds) {
			if (memberId == null) {
				continue;
			}
			chatParticipantRepository.findByRoomIdAndMemberId(roomId, memberId)
					.ifPresent(participant -> participant.markRead(messageId, readAt));
		}
	}

	@Transactional
	public void markReadToLatest(Long roomId, Long memberId) {
		if (roomId == null || memberId == null) {
			return;
		}
		ChatParticipant participant = chatParticipantRepository.findByRoomIdAndMemberId(roomId, memberId)
				.orElse(null);
		if (participant == null) {
			return;
		}
		Optional<ChatMessage> latestMessage = chatMessageRepository.findLatestMessage(roomId);
		latestMessage.ifPresent(message -> participant.markRead(message.getId(), LocalDateTime.now()));
	}

	public ChatMessageSliceResponse getMessages(Long roomId, Long requesterId, Long cursorId, Integer size) {
		if (roomId == null || requesterId == null) {
			throw new ChatInputInvalidException("roomId와 memberId는 필수입니다.");
		}
		int pageSize = size == null ? 20 : size;
		if (pageSize < 1 || pageSize > 100) {
			throw new ChatInputInvalidException("size는 1~100 사이여야 합니다.");
		}

		ChatRoom room = chatRoomRepository.findById(roomId)
				.orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다. roomId=" + roomId));

		ChatParticipant participant = chatParticipantRepository.findByRoomIdAndMemberId(room.getId(), requesterId)
				.orElseThrow(() -> new ChatAccessDeniedException("채팅방 참가자가 아닙니다. roomId=" + room.getId()));

		List<ChatMessage> messages = chatMessageRepository.findMessages(roomId, cursorId, pageSize + 1);
		boolean hasNext = messages.size() > pageSize;
		if (hasNext) {
			messages = messages.subList(0, pageSize);
		}

		Long nextCursorId = hasNext && !messages.isEmpty()
				? messages.get(messages.size() - 1).getId()
				: null;

		Collections.reverse(messages);
		List<ChatMessageResponse> responses = messages.stream()
				.map(ChatMessageResponse::from)
				.toList();

		markParticipantReadIfNeeded(participant, messages);

		return new ChatMessageSliceResponse(responses, nextCursorId, hasNext);
	}

	private void markParticipantReadIfNeeded(ChatParticipant participant, List<ChatMessage> messages) {
		if (participant == null) {
			return;
		}
		Long latestMessageId = null;
		if (!messages.isEmpty()) {
			latestMessageId = messages.get(messages.size() - 1).getId();
		} else {
			Optional<ChatMessage> latestMessage = chatMessageRepository.findLatestMessage(participant.getRoom().getId());
			if (latestMessage.isPresent()) {
				latestMessageId = latestMessage.get().getId();
			}
		}

		if (latestMessageId != null) {
			participant.markRead(latestMessageId, LocalDateTime.now());
		}
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
