package com.coc.modi.member.admin.notice.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.member.admin.notice.application.NoticeService;
import com.coc.modi.member.admin.notice.application.dto.NoticeResponse;
import com.coc.modi.member.admin.notice.presentation.dto.NoticeCreateRequest;
import com.coc.modi.member.admin.notice.presentation.dto.NoticeUpdateRequest;
import com.coc.modi.member.admin.exception.AdminAccessDeniedException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notices")
public class AdminNoticeController {

	private final NoticeService noticeService;

	@PostMapping
	public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(
			@AuthenticationPrincipal CustomMember member,
			@Valid @RequestBody NoticeCreateRequest request
	) {

		requireAdmin(member);
		NoticeResponse response = noticeService.createNotice(request.toCommand(member.memberId()));
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
	}

	@PatchMapping("/{noticeId}")
	public ResponseEntity<ApiResponse<NoticeResponse>> updateNotice(
			@AuthenticationPrincipal CustomMember member,
			@PathVariable Long noticeId,
			@Valid @RequestBody NoticeUpdateRequest request
	) {

		requireAdmin(member);
		NoticeResponse response = noticeService.updateNotice(request.toCommand(noticeId));
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@DeleteMapping("/{noticeId}")
	public ResponseEntity<Void> deleteNotice(
			@AuthenticationPrincipal CustomMember member,
			@PathVariable Long noticeId
	) {

		requireAdmin(member);
		noticeService.deleteNotice(noticeId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{noticeId}/publish")
	public ResponseEntity<ApiResponse<NoticeResponse>> publishNotice(
			@AuthenticationPrincipal CustomMember member,
			@PathVariable Long noticeId
	) {

		requireAdmin(member);
		return ResponseEntity.ok(ApiResponse.ok(noticeService.publishNotice(noticeId)));
	}

	@PatchMapping("/{noticeId}/draft")
	public ResponseEntity<ApiResponse<NoticeResponse>> draftNotice(
			@AuthenticationPrincipal CustomMember member,
			@PathVariable Long noticeId
	) {

		requireAdmin(member);
		return ResponseEntity.ok(ApiResponse.ok(noticeService.draftNotice(noticeId)));
	}

	private void requireAdmin(CustomMember member) {

		if (member == null || !"ADMIN".equals(member.role())) {
			throw new AdminAccessDeniedException("관리자 권한이 필요합니다.");
		}
	}
}
