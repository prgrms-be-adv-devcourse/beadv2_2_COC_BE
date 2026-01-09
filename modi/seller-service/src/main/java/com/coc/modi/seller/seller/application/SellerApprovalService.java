package com.coc.modi.seller.seller.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coc.modi.seller.seller.application.dto.SellerDetailResponse;
import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import com.coc.modi.seller.seller.exception.SellerNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerApprovalService {

	private final SellerRepository sellerRepository;

	@Transactional
	public SellerDetailResponse approveSeller(Long sellerId) {

		Seller seller = sellerRepository.findById(sellerId)
				.orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다. id=" + sellerId));

		seller.approve();
		sellerRepository.save(seller);

		return SellerDetailResponse.from(seller);
	}

	@Transactional
	public SellerDetailResponse rejectSeller(Long sellerId) {

		Seller seller = sellerRepository.findById(sellerId)
				.orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다. id=" + sellerId));

		seller.reject();
		sellerRepository.save(seller);

		return SellerDetailResponse.from(seller);
	}
}
