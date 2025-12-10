package com.coc.modi.seller.seller.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SellerRepository {

    Page<Seller> findAll(Pageable pageable);

    Optional<Seller> findById(Long sellerId);

    Optional<Seller> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);

    Seller save(Seller seller);

    void deleteById(Long sellerId);
}
