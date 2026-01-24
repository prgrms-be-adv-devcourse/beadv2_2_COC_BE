package com.coc.modi.admin.notice.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.admin.notice.application.NoticeService;
import com.coc.modi.admin.notice.application.dto.AdminNoticeSummaryResponse;
import com.coc.modi.admin.notice.application.dto.NoticeResponse;
import com.coc.modi.admin.notice.domain.NoticeStatus;
import com.coc.modi.admin.notice.presentation.dto.NoticeCreateRequest;
import com.coc.modi.admin.notice.presentation.dto.NoticeUpdateRequest;
import com.coc.modi.admin.exception.AdminAccessDeniedException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notices")
public class AdminNoticeController {

	private final NoticeService noticeService;

	@GetMapping
	public ResponseEntity<ApiResponse<Page<AdminNoticeSummaryResponse>>> getNotices(
			@AuthenticationPrincipal CustomMember member,
			@RequestParam(required = false) NoticeStatus status,
			@RequestParam(required = false) String keyword,
			@PageableDefault(sort = {"pinned", "createdAt"}, direction = Sort.Direction.DESC) Pageable pageable
	) {

		requireAdmin(member);
		Page<AdminNoticeSummaryResponse> response = noticeService.getAdminNotices(status, keyword, pageable);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@GetMapping("/{noticeId}")
	public ResponseEntity<ApiResponse<NoticeResponse>> getNotice(
			@AuthenticationPrincipal CustomMember member,
			@PathVariable Long noticeId
	) {

		requireAdmin(member);
		return ResponseEntity.ok(ApiResponse.ok(noticeService.getAdminNotice(noticeId)));
	}

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
