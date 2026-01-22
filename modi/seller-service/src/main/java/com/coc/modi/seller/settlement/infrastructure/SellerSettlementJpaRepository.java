package com.coc.modi.seller.settlement.infrastructure;

import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellerSettlementJpaRepository extends JpaRepository<SellerSettlement, Long> {

    Page<SellerSettlement> findBySellerId(Long sellerId, Pageable pageable);

    Page<SellerSettlement> findBySellerIdAndPeriodYm(Long sellerId, String periodYm, Pageable pageable);

    Page<SellerSettlement> findByPeriodYm(String periodYm, Pageable pageable);

    Optional<SellerSettlement> findBySellerIdAndPeriodYm(Long sellerId, String periodYm);

    List<SellerSettlement> findByBatchIdAndStatusOrderByIdAsc(Long batchId, SellerSettlementStatus status);

    @Query("""
            select s from SellerSettlement s
            where (:sellerId is null or s.sellerId = :sellerId)
              and (:periodYm is null or s.periodYm = :periodYm)
              and (:status is null or s.status = :status)
            """)
    Page<SellerSettlement> findByFilter(@Param("sellerId") Long sellerId,
                                        @Param("periodYm") String periodYm,
                                        @Param("status") SellerSettlementStatus status,
                                        Pageable pageable);

    @Query("""
            select s from SellerSettlement s
            where (:sellerId is null or s.sellerId = :sellerId)
              and (:periodYm is null or s.periodYm = :periodYm)
              and (:status is null or s.status = :status)
            """)
    List<SellerSettlement> findListByFilter(@Param("sellerId") Long sellerId,
                                            @Param("periodYm") String periodYm,
                                            @Param("status") SellerSettlementStatus status);
}
