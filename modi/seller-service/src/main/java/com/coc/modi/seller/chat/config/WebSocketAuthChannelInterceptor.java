package com.coc.modi.seller.chat.config;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.seller.chat.application.ChatMessageService;
import com.coc.modi.seller.chat.domain.ChatParticipantRepository;
import com.coc.modi.seller.chat.exception.ChatAccessDeniedException;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.security.core.Authentication;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

	private static final Pattern ROOM_TOPIC_PATTERN = Pattern.compile("^/topic/chat/rooms/(\\d+)$");

	private final ChatParticipantRepository chatParticipantRepository;
	private final ChatMessageService chatMessageService;
	private final ChatSubscriptionTracker chatSubscriptionTracker;

	public WebSocketAuthChannelInterceptor(ChatParticipantRepository chatParticipantRepository,
										   ChatMessageService chatMessageService,
										   ChatSubscriptionTracker chatSubscriptionTracker) {
		this.chatParticipantRepository = chatParticipantRepository;
		this.chatMessageService = chatMessageService;
		this.chatSubscriptionTracker = chatSubscriptionTracker;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || accessor.getCommand() == null) {
			return message;
		}

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			CustomMember member = resolveMember(accessor.getUser());
			if (member == null) {
				throw new ChatAccessDeniedException("웹소켓 인증 정보가 없습니다.");
			}
			chatSubscriptionTracker.registerSession(accessor.getSessionId(), member.memberId());
			return message;
		}

		if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
			Long roomId = extractRoomId(accessor.getDestination());
			if (roomId == null) {
				return message;
			}
			CustomMember member = resolveMember(accessor.getUser());
			if (member == null) {
				throw new ChatAccessDeniedException("웹소켓 인증 정보가 없습니다.");
			}
			chatParticipantRepository.findByRoomIdAndMemberId(roomId, member.memberId())
					.orElseThrow(() -> new ChatAccessDeniedException("채팅방 참가자가 아닙니다."));
			chatSubscriptionTracker.registerSubscription(accessor.getSessionId(), accessor.getSubscriptionId(), roomId,
					member.memberId());
			chatMessageService.markReadToLatest(roomId, member.memberId());
			return message;
		}

		if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
			chatSubscriptionTracker.unregisterSubscription(accessor.getSessionId(), accessor.getSubscriptionId());
			return message;
		}

		if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
			chatSubscriptionTracker.unregisterSession(accessor.getSessionId());
		}

		return message;
	}

	private Long extractRoomId(String destination) {
		if (destination == null) {
			return null;
		}
		Matcher matcher = ROOM_TOPIC_PATTERN.matcher(destination);
		if (matcher.matches()) {
			return Long.valueOf(matcher.group(1));
		}
		return null;
	}

	private CustomMember resolveMember(Principal principal) {
		if (principal instanceof Authentication authentication
				&& authentication.getPrincipal() instanceof CustomMember member) {
			return member;
		}

		return null;
	}
}
