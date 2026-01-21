package com.coc.modi.seller.seller.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.coc.modi.kafka.event.SellerApprovedEvent;
import com.coc.modi.kafka.event.SellerRejectedEvent;
import com.coc.modi.seller.outbox.SellerOutboxService;
import com.coc.modi.seller.seller.application.dto.SellerDetailResponse;
import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import com.coc.modi.seller.seller.domain.SellerStatus;
import com.coc.modi.seller.seller.exception.SellerNotFoundException;
import com.coc.modi.seller.seller.infrastructure.client.member.MemberClientAdapter;
import com.coc.modi.seller.seller.infrastructure.client.member.dto.MemberEmailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerApprovalService {

	private final SellerRepository sellerRepository;
	private final SellerOutboxService sellerOutboxService;
	private final MemberClientAdapter memberClientAdapter;

	@Transactional
	public SellerDetailResponse approveSeller(Long sellerId) {

		Seller seller = sellerRepository.findById(sellerId)
				.orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다. id=" + sellerId));

		boolean wasPending = seller.getStatus() == SellerStatus.PENDING;
		seller.approve();
		sellerRepository.save(seller);

		if (wasPending) {
			MemberEmailResponse emailResponse = memberClientAdapter.getMemberEmail(seller.getMemberId());
			String email = emailResponse != null ? emailResponse.email() : null;
			if (!StringUtils.hasText(email)) {
				log.warn("승인 메일 발송을 위한 이메일이 비어있습니다. sellerId={}, memberId={}",
						seller.getId(), seller.getMemberId());
			}
			sellerOutboxService.enqueueSellerApproved(
					SellerApprovedEvent.of(seller.getId(), seller.getMemberId(), email)
			);
		}

		return SellerDetailResponse.from(seller);
	}

	@Transactional
	public SellerDetailResponse rejectSeller(Long sellerId) {

		Seller seller = sellerRepository.findById(sellerId)
				.orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다. id=" + sellerId));

		boolean wasPending = seller.getStatus() == SellerStatus.PENDING;
		seller.reject();
		sellerRepository.save(seller);

		if (wasPending) {
			MemberEmailResponse emailResponse = memberClientAdapter.getMemberEmail(seller.getMemberId());
			String email = emailResponse != null ? emailResponse.email() : null;
			if (!StringUtils.hasText(email)) {
				log.warn("거절 메일 발송을 위한 이메일이 비어있습니다. sellerId={}, memberId={}",
						seller.getId(), seller.getMemberId());
			}
			sellerOutboxService.enqueueSellerRejected(
					SellerRejectedEvent.of(seller.getId(), seller.getMemberId(), email)
			);
		}

		return SellerDetailResponse.from(seller);
	}
}
