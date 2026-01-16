package com.coc.modi.seller.chat.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.seller.chat.application.ChatRoomService;
import com.coc.modi.seller.chat.application.dto.ChatRoomResponse;
import com.coc.modi.seller.chat.presentation.dto.ChatRoomCreateRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {

	private final ChatRoomService chatRoomService;

	@PostMapping
	public ResponseEntity<ApiResponse<ChatRoomResponse>> createRoom(
			@Valid @RequestBody ChatRoomCreateRequest request,
			@AuthenticationPrincipal CustomMember member
	) {
		ChatRoomResponse response = chatRoomService.createRoom(request.toCommand(member));
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@GetMapping("/{roomId}")
	public ResponseEntity<ApiResponse<ChatRoomResponse>> getRoom(
			@PathVariable Long roomId,
			@AuthenticationPrincipal CustomMember member
	) {
		ChatRoomResponse response = chatRoomService.getRoom(roomId, member.memberId());
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
}
