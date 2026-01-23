package com.coc.modi.admin.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.admin.application.AdminMemberService;
import com.coc.modi.admin.application.dto.AdminMemberCreateResponse;
import com.coc.modi.admin.presentation.dto.AdminMemberCreateRequest;
import com.coc.modi.admin.exception.AdminAccessDeniedException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/members")
public class AdminMemberController {

	private final AdminMemberService adminMemberService;

	@PostMapping
	public ResponseEntity<ApiResponse<AdminMemberCreateResponse>> createAdmin(
			@AuthenticationPrincipal CustomMember member,
			@Valid @RequestBody AdminMemberCreateRequest request
	) {

		requireAdmin(member);
		AdminMemberCreateResponse response = adminMemberService.createAdmin(
				request.toCommand(),
				member.memberId()
		);

		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	private void requireAdmin(CustomMember member) {

		if (member == null || !"ADMIN".equals(member.role())) {
			throw new AdminAccessDeniedException("관리자 권한이 필요합니다.");
		}
	}
}
