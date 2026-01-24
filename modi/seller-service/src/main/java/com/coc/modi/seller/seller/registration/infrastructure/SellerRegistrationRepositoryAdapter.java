package com.coc.modi.seller.seller.registration.infrastructure;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.coc.modi.seller.seller.registration.domain.SellerRegistration;
import com.coc.modi.seller.seller.registration.domain.SellerRegistrationRepository;
import com.coc.modi.seller.seller.registration.domain.SellerRegistrationStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SellerRegistrationRepositoryAdapter implements SellerRegistrationRepository {

	private final SellerRegistrationJpaRepository sellerRegistrationJpaRepository;

	@Override
	public Optional<SellerRegistration> findByMemberId(Long memberId) {

		return sellerRegistrationJpaRepository.findByMemberId(memberId);
	}

	@Override
	public SellerRegistration save(SellerRegistration registration) {

		return sellerRegistrationJpaRepository.save(registration);
	}

	@Override
	public Page<SellerRegistration> findAll(Pageable pageable) {
		return sellerRegistrationJpaRepository.findAll(pageable);
	}

	@Override
	public Page<SellerRegistration> findByStatus(SellerRegistrationStatus status, Pageable pageable) {
		return sellerRegistrationJpaRepository.findByStatus(status, pageable);
	}
}
