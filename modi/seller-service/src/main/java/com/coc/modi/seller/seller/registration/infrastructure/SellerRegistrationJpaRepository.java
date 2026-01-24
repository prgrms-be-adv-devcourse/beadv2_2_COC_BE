package com.coc.modi.seller.seller.registration.infrastructure;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.coc.modi.seller.seller.registration.domain.SellerRegistration;
import com.coc.modi.seller.seller.registration.domain.SellerRegistrationStatus;

public interface SellerRegistrationJpaRepository extends JpaRepository<SellerRegistration, Long> {

	Optional<SellerRegistration> findByMemberId(Long memberId);

	Page<SellerRegistration> findByStatus(SellerRegistrationStatus status, Pageable pageable);
}
