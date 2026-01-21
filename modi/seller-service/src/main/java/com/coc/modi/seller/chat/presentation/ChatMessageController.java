package com.coc.modi.seller.chat.presentation;

import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.seller.chat.application.ChatMessageService;
import com.coc.modi.seller.chat.application.dto.ChatMessageResponse;
import com.coc.modi.seller.chat.presentation.dto.ChatMessageSendRequest;
import com.coc.modi.seller.chat.exception.ChatAccessDeniedException;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

	private final ChatMessageService chatMessageService;

	@MessageMapping("/chat/rooms/{roomId}/send")
	public void sendMessage(@DestinationVariable Long roomId,
							ChatMessageSendRequest request,
							Principal principal) {
		CustomMember sender = resolveSender(principal);
		chatMessageService.sendMessage(request.toCommand(roomId, sender));
	}

	private CustomMember resolveSender(Principal principal) {
		if (principal instanceof Authentication authentication
				&& authentication.getPrincipal() instanceof CustomMember member) {
			return member;
		}

		throw new ChatAccessDeniedException("메시지 전송 권한이 없습니다.");
	}
}
