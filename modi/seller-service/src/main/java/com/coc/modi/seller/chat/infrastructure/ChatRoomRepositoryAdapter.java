package com.coc.modi.seller.chat.infrastructure;

import com.coc.modi.seller.chat.domain.ChatRoom;
import com.coc.modi.seller.chat.domain.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryAdapter implements ChatRoomRepository {

	private final ChatRoomJpaRepository chatRoomJpaRepository;

	@Override
	public Optional<ChatRoom> findById(Long roomId) {

		return chatRoomJpaRepository.findById(roomId);
	}

	@Override
	public Optional<ChatRoom> findByRoomKey(String roomKey) {

		return chatRoomJpaRepository.findByRoomKey(roomKey);
	}

	@Override
	public ChatRoom save(ChatRoom room) {

		return chatRoomJpaRepository.save(room);
	}
}
