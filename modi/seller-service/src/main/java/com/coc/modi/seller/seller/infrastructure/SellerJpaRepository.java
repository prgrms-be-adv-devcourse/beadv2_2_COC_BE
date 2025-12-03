package com.coc.modi.seller.seller.infrastructure;

import com.coc.modi.seller.seller.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerJpaRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);
}
