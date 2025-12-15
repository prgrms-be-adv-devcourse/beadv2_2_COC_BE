package com.coc.modi.seller.seller.domain;

import java.util.Optional;
import java.util.List;

public interface SellerRepository {
	
	
	Optional<Seller> findById(Long sellerId);
	
	Optional<Seller> findByMemberId(Long memberId);
	
	boolean existsByMemberId(Long memberId);
	
	Seller save(Seller seller);
	
	List<Seller> findByStatus(SellerStatus status);
}
