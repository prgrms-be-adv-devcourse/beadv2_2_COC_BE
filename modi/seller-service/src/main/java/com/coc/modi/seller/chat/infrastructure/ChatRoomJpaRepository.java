package com.coc.modi.seller.chat.infrastructure;

import com.coc.modi.seller.chat.domain.ChatRoom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, Long> {

	Optional<ChatRoom> findByRoomKey(String roomKey);
}
