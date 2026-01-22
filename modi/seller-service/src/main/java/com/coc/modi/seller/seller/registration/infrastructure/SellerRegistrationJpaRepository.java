package com.coc.modi.seller.seller.registration.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coc.modi.seller.seller.registration.domain.SellerRegistration;

public interface SellerRegistrationJpaRepository extends JpaRepository<SellerRegistration, Long> {

	Optional<SellerRegistration> findByMemberId(Long memberId);
}
