package com.coc.modi.admin.seller.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.coc.modi.admin.seller.application.dto.SellerRegistrationResponse;
import com.coc.modi.admin.seller.domain.SellerRegistrationStatus;
import com.coc.modi.admin.seller.infrastructure.client.SellerApprovalClient;
import com.coc.modi.admin.seller.infrastructure.client.dto.SellerRegistrationPageRequest;
import com.coc.modi.admin.seller.infrastructure.client.dto.SellerRegistrationPageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerApprovalAdminService {

	private final SellerApprovalClient sellerApprovalClient;

	public SellerRegistrationResponse approveSeller(Long memberId, Long approvedBy) {

		if (memberId == null) {
			throw new IllegalArgumentException("memberId는 필수입니다.");
		}
		if (approvedBy == null) {
			throw new IllegalArgumentException("approvedBy는 필수입니다.");
		}

		return sellerApprovalClient.approveSeller(memberId, approvedBy);
	}

	public SellerRegistrationResponse rejectSeller(Long memberId) {

		if (memberId == null) {
			throw new IllegalArgumentException("memberId는 필수입니다.");
		}

		return sellerApprovalClient.rejectSeller(memberId);
	}

	public Page<SellerRegistrationResponse> getRegistrations(
			SellerRegistrationStatus status,
			Pageable pageable
	) {

		SellerRegistrationPageRequest request = SellerRegistrationPageRequest.from(status, pageable);
		SellerRegistrationPageResponse response = sellerApprovalClient.getRegistrations(request);

		PageRequest pageRequest = pageable == null
				? PageRequest.of(response.page(), response.size())
				: PageRequest.of(response.page(), response.size(), pageable.getSort());
		return new PageImpl<>(response.content(), pageRequest, response.totalElements());
	}
}
