package com.coc.modi.seller.chat.infrastructure;

import com.coc.modi.seller.chat.domain.ChatMessage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findByRoomIdOrderByIdDesc(Long roomId, Pageable pageable);

	List<ChatMessage> findByRoomIdAndIdLessThanOrderByIdDesc(Long roomId, Long cursorId, Pageable pageable);
}
