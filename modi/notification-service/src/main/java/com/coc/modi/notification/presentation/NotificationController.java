package com.coc.modi.notification.presentation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.notification.application.NotificationSseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
	
	private final NotificationSseService notificationSseService;
	
	@GetMapping("/stream")
	public SseEmitter stream(@AuthenticationPrincipal CustomMember member) {
		
		return notificationSseService.subscribe(member.memberId());
	}
}
