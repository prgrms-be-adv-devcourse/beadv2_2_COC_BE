package com.coc.modi.seller.chat.infrastructure;

import com.coc.modi.seller.chat.domain.ChatMessage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessage, Long> {
}
