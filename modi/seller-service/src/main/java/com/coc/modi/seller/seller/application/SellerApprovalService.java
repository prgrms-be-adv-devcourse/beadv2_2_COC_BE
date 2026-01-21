package com.coc.modi.seller.seller.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.coc.modi.kafka.event.SellerApprovedEvent;
import com.coc.modi.kafka.event.SellerRegistrationRejectedEvent;
import com.coc.modi.seller.outbox.SellerOutboxService;
import com.coc.modi.seller.seller.application.dto.SellerRegistrationResponse;
import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import com.coc.modi.seller.seller.exception.SellerStatusConflictException;
import com.coc.modi.seller.seller.infrastructure.client.member.MemberClientAdapter;
import com.coc.modi.seller.seller.infrastructure.client.member.dto.MemberEmailResponse;
import com.coc.modi.seller.seller.registration.domain.SellerRegistration;
import com.coc.modi.seller.seller.registration.domain.SellerRegistrationRepository;
import com.coc.modi.seller.seller.registration.domain.SellerRegistrationStatus;
import com.coc.modi.seller.seller.registration.exception.SellerRegistrationNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerApprovalService {

	private final SellerRepository sellerRepository;
	private final SellerRegistrationRepository sellerRegistrationRepository;
	private final SellerOutboxService sellerOutboxService;
	private final MemberClientAdapter memberClientAdapter;

	@Transactional
	public SellerRegistrationResponse approveSeller(Long memberId, Long approvedBy) {

		SellerRegistration registration = sellerRegistrationRepository.findByMemberId(memberId)
				.orElseThrow(() -> new SellerRegistrationNotFoundException("판매자 등록 요청을 찾을 수 없습니다. memberId=" + memberId));

		if (registration.getStatus() == SellerRegistrationStatus.APPROVED) {
			throw new SellerStatusConflictException("seller registration is already approved. memberId=" + memberId);
		}
		if (registration.getStatus() != SellerRegistrationStatus.PENDING) {
			throw new SellerStatusConflictException(
					"seller registration approval is only allowed from PENDING. status=" + registration.getStatus()
			);
		}
		if (sellerRepository.existsByMemberId(memberId)) {
			throw new SellerStatusConflictException("seller is already registered. memberId=" + memberId);
		}

		Seller seller = Seller.create(
				registration.getMemberId(),
				registration.getStoreName(),
				registration.getBizRegNo(),
				registration.getStorePhone()
		);
		seller.approve();
		sellerRepository.save(seller);

		registration.approve(approvedBy);
		sellerRegistrationRepository.save(registration);

		MemberEmailResponse emailResponse = memberClientAdapter.getMemberEmail(seller.getMemberId());
		String email = emailResponse != null ? emailResponse.email() : null;
		if (!StringUtils.hasText(email)) {
			log.warn("승인 메일 발송을 위한 이메일이 비어있습니다. sellerId={}, memberId={}",
					seller.getId(), seller.getMemberId());
		}
		sellerOutboxService.enqueueSellerApproved(
				SellerApprovedEvent.of(seller.getId(), seller.getMemberId(), email)
		);

		return SellerRegistrationResponse.from(registration);
	}

	@Transactional
	public SellerRegistrationResponse rejectSeller(Long memberId) {

		SellerRegistration registration = sellerRegistrationRepository.findByMemberId(memberId)
				.orElseThrow(() -> new SellerRegistrationNotFoundException("판매자 등록 요청을 찾을 수 없습니다. memberId=" + memberId));

		if (registration.getStatus() == SellerRegistrationStatus.REJECTED) {
			return SellerRegistrationResponse.from(registration);
		}
		if (registration.getStatus() != SellerRegistrationStatus.PENDING) {
			throw new SellerStatusConflictException(
					"seller registration rejection is only allowed from PENDING. status=" + registration.getStatus()
			);
		}

		registration.reject();
		sellerRegistrationRepository.save(registration);

		MemberEmailResponse emailResponse = memberClientAdapter.getMemberEmail(registration.getMemberId());
		String email = emailResponse != null ? emailResponse.email() : null;
		if (!StringUtils.hasText(email)) {
			log.warn("거절 메일 발송을 위한 이메일이 비어있습니다. registrationId={}, memberId={}",
					registration.getId(), registration.getMemberId());
		}
		sellerOutboxService.enqueueSellerRejected(
				SellerRegistrationRejectedEvent.of(registration.getId(), registration.getMemberId(), email)
		);

		return SellerRegistrationResponse.from(registration);
	}
}
