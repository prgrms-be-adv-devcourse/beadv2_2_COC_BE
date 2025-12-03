package com.coc.modi.seller.settlement.infrastructure;

import com.coc.modi.seller.settlement.domain.SellerSettlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerSettlementJpaRepository extends JpaRepository<SellerSettlement, Long> {

    Page<SellerSettlement> findBySellerId(Long sellerId, Pageable pageable);
}
