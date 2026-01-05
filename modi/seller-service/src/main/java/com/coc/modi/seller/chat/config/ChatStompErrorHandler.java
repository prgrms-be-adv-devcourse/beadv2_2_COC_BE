package com.coc.modi.seller.chat.config;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.seller.chat.exception.ChatAccessDeniedException;
import com.coc.modi.seller.chat.presentation.dto.ChatErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

public class ChatStompErrorHandler extends StompSubProtocolErrorHandler {

	private final ObjectMapper objectMapper;

	public ChatStompErrorHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
		ChatErrorResponse response = resolveErrorResponse(ex);
		return buildErrorMessage(response);
	}

	private Message<byte[]> buildErrorMessage(ChatErrorResponse response) {
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
		accessor.setLeaveMutable(true);
		accessor.setMessage(response.message());
		accessor.setMessageTypeIfNotSet(SimpMessageType.MESSAGE);
		accessor.setContentType(MimeTypeUtils.APPLICATION_JSON);

		byte[] payload = serialize(response);
		return MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
	}

	private ChatErrorResponse resolveErrorResponse(Throwable ex) {
		Throwable current = ex;
		while (current != null) {
			if (current instanceof ChatAccessDeniedException chatEx) {
				return new ChatErrorResponse(chatEx.getErrorCode().getCode(), chatEx.getDetailMessage());
			}
			if (current instanceof org.springframework.security.access.AccessDeniedException accessDenied) {
				return new ChatErrorResponse(ErrorCode.CHAT_FORBIDDEN.getCode(),
						accessDenied.getMessage() == null ? ErrorCode.CHAT_FORBIDDEN.getDefaultMessage() : accessDenied.getMessage());
			}
			current = current.getCause();
		}
		return new ChatErrorResponse(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getDefaultMessage());
	}

	private byte[] serialize(ChatErrorResponse response) {
		try {
			return objectMapper.writeValueAsBytes(response);
		} catch (JsonProcessingException e) {
			return new byte[0];
		}
	}
}
