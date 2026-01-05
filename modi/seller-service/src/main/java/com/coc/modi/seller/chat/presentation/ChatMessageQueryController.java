package com.coc.modi.seller.chat.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.seller.chat.application.ChatMessageService;
import com.coc.modi.seller.chat.application.dto.ChatMessageSliceResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms/{roomId}/messages")
public class ChatMessageQueryController {

	private final ChatMessageService chatMessageService;

	@GetMapping
	public ResponseEntity<ApiResponse<ChatMessageSliceResponse>> getMessages(
			@PathVariable Long roomId,
			@AuthenticationPrincipal CustomMember member,
			@RequestParam(value = "cursorId", required = false) Long cursorId,
			@RequestParam(value = "size", required = false) Integer size
	) {
		ChatMessageSliceResponse response = chatMessageService.getMessages(roomId, member.memberId(), cursorId, size);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
}
