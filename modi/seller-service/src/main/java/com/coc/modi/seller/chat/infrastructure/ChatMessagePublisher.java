package com.coc.modi.seller.chat.infrastructure;

import com.coc.modi.seller.chat.application.dto.ChatMessageEvent;

public interface ChatMessagePublisher {

	void publish(ChatMessageEvent event);
}
