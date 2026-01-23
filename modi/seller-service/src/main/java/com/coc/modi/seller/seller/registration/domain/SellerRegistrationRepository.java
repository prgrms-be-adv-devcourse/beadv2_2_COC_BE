package com.coc.modi.seller.seller.registration.domain;

import java.util.Optional;

public interface SellerRegistrationRepository {

	Optional<SellerRegistration> findByMemberId(Long memberId);

	SellerRegistration save(SellerRegistration registration);
}
