package com.coc.modi.seller.seller.registration.domain;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SellerRegistrationRepository {

	Optional<SellerRegistration> findByMemberId(Long memberId);

	SellerRegistration save(SellerRegistration registration);

	Page<SellerRegistration> findAll(Pageable pageable);

	Page<SellerRegistration> findByStatus(SellerRegistrationStatus status, Pageable pageable);
}
