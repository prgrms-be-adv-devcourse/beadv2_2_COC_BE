package com.coc.modi.seller.seller.infrastructure;

import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import com.coc.modi.seller.seller.domain.SellerStatus;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SellerRepositoryAdapter implements SellerRepository {
	
	private final SellerJpaRepository sellerJpaRepository;
	
	@Override
	public Optional<Seller> findById(Long sellerId) {
		
		return sellerJpaRepository.findById(sellerId);
	}
	
	@Override
	public Optional<Seller> findByMemberId(Long memberId) {
		
		return sellerJpaRepository.findByMemberId(memberId);
	}
	
	@Override
	public boolean existsByMemberId(Long memberId) {
		
		return sellerJpaRepository.existsByMemberId(memberId);
	}
	
	@Override
	public Seller save(Seller seller) {
		
		return sellerJpaRepository.save(seller);
	}
	
	@Override
	public List<Seller> findByStatus(SellerStatus status) {
		
		return sellerJpaRepository.findByStatus(status);
	}
}
