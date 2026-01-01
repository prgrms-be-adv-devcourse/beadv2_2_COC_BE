package com.coc.modi.seller.chat.domain;

public interface ChatMessageRepository {

	ChatMessage save(ChatMessage message);

	java.util.List<ChatMessage> findMessages(Long roomId, Long cursorId, int size);
}
