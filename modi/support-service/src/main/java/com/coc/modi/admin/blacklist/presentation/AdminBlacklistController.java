package com.coc.modi.admin.blacklist.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.admin.blacklist.application.BlacklistService;
import com.coc.modi.admin.blacklist.application.dto.BlacklistDetailResponse;
import com.coc.modi.admin.blacklist.application.dto.BlacklistSummaryResponse;
import com.coc.modi.admin.blacklist.domain.BlacklistStatus;
import com.coc.modi.admin.blacklist.presentation.dto.BlacklistReleaseRequest;
import com.coc.modi.admin.blacklist.presentation.dto.BlacklistSuspendRequest;
import com.coc.modi.admin.exception.AdminAccessDeniedException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/blacklists")
public class AdminBlacklistController {

	private final BlacklistService blacklistService;

	@GetMapping
	public ResponseEntity<ApiResponse<Page<BlacklistSummaryResponse>>> getBlacklists(
			@AuthenticationPrincipal CustomMember member,
			@RequestParam(required = false) BlacklistStatus status,
			@PageableDefault Pageable pageable
	) {

		requireAdmin(member);
		Page<BlacklistSummaryResponse> responses =
				blacklistService.getBlacklists(status, pageable);
		return ResponseEntity.ok(ApiResponse.ok(responses));
	}

	@GetMapping("/search")
	public ResponseEntity<ApiResponse<BlacklistSummaryResponse>> searchByEmail(
			@AuthenticationPrincipal CustomMember member,
			@RequestParam @NotBlank String email
	) {

		requireAdmin(member);
		BlacklistSummaryResponse response = blacklistService.searchByEmail(email);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@GetMapping("/{memberId}")
	public ResponseEntity<ApiResponse<BlacklistDetailResponse>> getBlacklistDetail(
			@AuthenticationPrincipal CustomMember member,
			@PathVariable Long memberId
	) {

		requireAdmin(member);
		BlacklistDetailResponse response = blacklistService.getBlacklistDetail(memberId);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<BlacklistDetailResponse>> suspend(
			@AuthenticationPrincipal CustomMember member,
			@Valid @RequestBody BlacklistSuspendRequest request
	) {

		requireAdmin(member);
		BlacklistDetailResponse response = blacklistService.suspend(request.toCommand(member.memberId()));
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
	}

	@PatchMapping("/{memberId}/release")
	public ResponseEntity<ApiResponse<BlacklistDetailResponse>> release(
			@AuthenticationPrincipal CustomMember member,
			@PathVariable Long memberId,
			@Valid @RequestBody(required = false) BlacklistReleaseRequest request
	) {

		requireAdmin(member);
		BlacklistReleaseRequest releaseRequest = (request == null)
				? new BlacklistReleaseRequest(null)
				: request;
		BlacklistDetailResponse response =
				blacklistService.release(releaseRequest.toCommand(memberId, member.memberId()));
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	private void requireAdmin(CustomMember member) {

		if (member == null || !"ADMIN".equals(member.role())) {
			throw new AdminAccessDeniedException("관리자 권한이 필요합니다.");
		}
	}
}
