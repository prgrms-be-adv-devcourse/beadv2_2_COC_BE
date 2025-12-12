package com.coc.modi.seller.seller.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;

public interface SellerRepository {
	
	Page<Seller> findAll(Pageable pageable);
	
	Optional<Seller> findById(Long sellerId);
	
	Optional<Seller> findByMemberId(Long memberId);
	
	boolean existsByMemberId(Long memberId);
	
	Seller save(Seller seller);
	
	List<Seller> findByStatus(SellerStatus status);
}
