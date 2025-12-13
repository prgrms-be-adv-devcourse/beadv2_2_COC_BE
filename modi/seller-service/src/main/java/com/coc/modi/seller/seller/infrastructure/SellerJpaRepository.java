package com.coc.modi.seller.seller.infrastructure;

import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface SellerJpaRepository extends JpaRepository<Seller, Long> {
	
	Optional<Seller> findByMemberId(Long memberId);
	
	boolean existsByMemberId(Long memberId);
	
	List<Seller> findByStatus(SellerStatus status);
}
