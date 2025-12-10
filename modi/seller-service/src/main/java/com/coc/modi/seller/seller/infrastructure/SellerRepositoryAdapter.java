package com.coc.modi.seller.seller.infrastructure;

import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SellerRepositoryAdapter implements SellerRepository {

    private final SellerJpaRepository sellerJpaRepository;

    @Override
    public Page<Seller> findAll(Pageable pageable) {
        return sellerJpaRepository.findAll(pageable);
    }

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
    public void deleteById(Long sellerId) {
        sellerJpaRepository.deleteById(sellerId);
    }
}
