package com.coc.modi.seller.seller.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coc.modi.kafka.event.SellerApprovedEvent;
import com.coc.modi.kafka.event.SellerRejectedEvent;
import com.coc.modi.seller.outbox.SellerOutboxService;
import com.coc.modi.seller.seller.application.dto.SellerDetailResponse;
import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import com.coc.modi.seller.seller.domain.SellerStatus;
import com.coc.modi.seller.seller.exception.SellerNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerApprovalService {

	private final SellerRepository sellerRepository;
	private final SellerOutboxService sellerOutboxService;

	@Transactional
	public SellerDetailResponse approveSeller(Long sellerId) {

		Seller seller = sellerRepository.findById(sellerId)
				.orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다. id=" + sellerId));

		boolean wasPending = seller.getStatus() == SellerStatus.PENDING;
		seller.approve();
		sellerRepository.save(seller);

		if (wasPending) {
			sellerOutboxService.enqueueSellerApproved(
					SellerApprovedEvent.of(seller.getId(), seller.getMemberId())
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
			sellerOutboxService.enqueueSellerRejected(
					SellerRejectedEvent.of(seller.getId(), seller.getMemberId())
			);
		}

		return SellerDetailResponse.from(seller);
	}
}
