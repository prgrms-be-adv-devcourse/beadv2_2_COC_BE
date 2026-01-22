package com.coc.modi.seller.settlement.infrastructure;

import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementRepository;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SellerSettlementRepositoryAdapter implements SellerSettlementRepository {
	
	private final SellerSettlementJpaRepository sellerSettlementJpaRepository;
	
	@Override
	public Page<SellerSettlement> findBySellerId(Long sellerId, Pageable pageable) {
		
		return sellerSettlementJpaRepository.findBySellerId(sellerId, pageable);
	}
	
	@Override
	public Page<SellerSettlement> findBySellerIdAndPeriodYm(Long sellerId, String periodYm, Pageable pageable) {
		
		return sellerSettlementJpaRepository.findBySellerIdAndPeriodYm(sellerId, periodYm, pageable);
	}

	@Override
	public Page<SellerSettlement> findByPeriodYm(String periodYm, Pageable pageable) {

		return sellerSettlementJpaRepository.findByPeriodYm(periodYm, pageable);
	}

	@Override
	public Page<SellerSettlement> findAll(Pageable pageable) {

		return sellerSettlementJpaRepository.findAll(pageable);
	}

	@Override
	public Page<SellerSettlement> findByFilter(Long sellerId,
											   String periodYm,
											   SellerSettlementStatus status,
											   Pageable pageable) {

		return sellerSettlementJpaRepository.findByFilter(sellerId, periodYm, status, pageable);
	}

	@Override
	public List<SellerSettlement> findListByFilter(Long sellerId,
												   String periodYm,
												   SellerSettlementStatus status) {

		return sellerSettlementJpaRepository.findListByFilter(sellerId, periodYm, status);
	}
	
	@Override
	public Optional<SellerSettlement> findBySellerIdAndPeriodYm(Long sellerId, String periodYm) {
		
		return sellerSettlementJpaRepository.findBySellerIdAndPeriodYm(sellerId, periodYm);
	}
	
	@Override
	public Optional<SellerSettlement> findById(Long sellerSettlementId) {

		return sellerSettlementJpaRepository.findById(sellerSettlementId);
	}
	
	@Override
	public SellerSettlement save(SellerSettlement settlement) {
		
		return sellerSettlementJpaRepository.save(settlement);
	}
}
