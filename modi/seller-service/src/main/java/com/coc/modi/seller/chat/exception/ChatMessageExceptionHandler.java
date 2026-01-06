package com.coc.modi.seller.chat.exception;

import com.coc.modi.seller.chat.presentation.dto.ChatErrorResponse;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ChatMessageExceptionHandler {

	@MessageExceptionHandler(ChatInputInvalidException.class)
	@SendToUser("/queue/errors")
	public ChatErrorResponse handleChatInputInvalid(ChatInputInvalidException ex) {
		return new ChatErrorResponse(ex.getErrorCode().getCode(), ex.getDetailMessage());
	}
}
