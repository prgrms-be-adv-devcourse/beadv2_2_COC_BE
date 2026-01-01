package com.coc.modi.seller.chat.infrastructure;

import com.coc.modi.seller.chat.domain.ChatMessage;
import com.coc.modi.seller.chat.domain.ChatMessageRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryAdapter implements ChatMessageRepository {

    private final ChatMessageJpaRepository chatMessageJpaRepository;

    @Override
    public ChatMessage save(ChatMessage message) {

		return chatMessageJpaRepository.save(message);
    }

	@Override
	public List<ChatMessage> findMessages(Long roomId, Long cursorId, int size) {
		PageRequest pageable = PageRequest.of(0, size);
		if (cursorId == null) {
			return chatMessageJpaRepository.findByRoomIdOrderByIdDesc(roomId, pageable);
		}
		return chatMessageJpaRepository.findByRoomIdAndIdLessThanOrderByIdDesc(roomId, cursorId, pageable);
	}

	@Override
	public java.util.Optional<ChatMessage> findLatestMessage(Long roomId) {
		return chatMessageJpaRepository.findFirstByRoomIdOrderByIdDesc(roomId);
	}
}
