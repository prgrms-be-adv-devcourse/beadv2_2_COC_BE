package com.coc.modi.product.product.presentation.admin;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.product.product.application.ProductModerationAdminService;
import com.coc.modi.product.product.application.dto.ProductModerationSummaryResponse;
import com.coc.modi.product.product.domain.ProductModerationStatus;
import com.coc.modi.product.product.exception.ProductAccessDeniedException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
public class ProductModerationAdminController {

	private final ProductModerationAdminService productModerationAdminService;

	@GetMapping("/moderation-requests")
	public ResponseEntity<ApiResponse<Page<ProductModerationSummaryResponse>>> getModerationRequests(
			@AuthenticationPrincipal CustomMember member,
			@RequestParam(name = "moderationStatus", defaultValue = "PENDING") ProductModerationStatus moderationStatus,
			@PageableDefault(
					size = 20,
					sort = "createdAt",
					direction = Sort.Direction.ASC
			) Pageable pageable
	) {

		requireAdmin(member);
		Page<ProductModerationSummaryResponse> response =
				productModerationAdminService.getModerationRequests(moderationStatus, pageable);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@PostMapping("/{productId}/moderation-requests")
	public ResponseEntity<ApiResponse<Void>> requestModeration(
			@AuthenticationPrincipal CustomMember member,
			@PathVariable Long productId
	) {

		requireAdmin(member);
		productModerationAdminService.requestModeration(productId);
		return ResponseEntity.ok(ApiResponse.ok(null));
	}

	private void requireAdmin(CustomMember member) {

		if (member == null || !"ADMIN".equals(member.role())) {
			throw new ProductAccessDeniedException("관리자 접근");
		}
	}
}
