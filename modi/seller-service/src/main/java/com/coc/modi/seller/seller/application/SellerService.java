package com.coc.modi.seller.seller.application;

import com.coc.modi.seller.seller.application.dto.SellerCreateCommand;
import com.coc.modi.seller.seller.application.dto.SellerInfo;
import com.coc.modi.seller.seller.application.dto.SellerUpdateCommand;
import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;

    @Transactional(readOnly = true)
    public Page<SellerInfo> findSellers(Pageable pageable) {
        return sellerRepository.findAll(pageable)
                .map(SellerInfo::from);
    }

    @Transactional(readOnly = true)
    public SellerInfo getSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다."));
        return SellerInfo.from(seller);
    }

    @Transactional(readOnly = true)
    public SellerInfo getSellerByMemberId(Long memberId) {
        Seller seller = sellerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다"));
        return SellerInfo.from(seller);
    }

    @Transactional
    public SellerInfo registerSeller(SellerCreateCommand command) {
        if (sellerRepository.existsByMemberId(command.memberId())) {
            throw new IllegalArgumentException("이미 등록된 판매자입니다.");
        }

        Seller seller = Seller.create(
                command.memberId(),
                command.storeName(),
                command.bizRegNo(),
                command.storePhone()
        );

        Seller saved = sellerRepository.save(seller);
        return SellerInfo.from(saved);
    }

    @Transactional
    public SellerInfo updateSeller(Long sellerId, SellerUpdateCommand command) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다"));

        seller.update(
                command.storeName(),
                command.bizRegNo(),
                command.storePhone()
        );
        seller.changeStatus(command.status());

        return SellerInfo.from(seller);
    }

    @Transactional
    public SellerInfo updateSellerByMemberId(Long memberId, SellerUpdateCommand command) {
        Seller seller = sellerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다"));

        seller.update(
                command.storeName(),
                command.bizRegNo(),
                command.storePhone()
        );
        seller.changeStatus(command.status());

        return SellerInfo.from(seller);
    }

    @Transactional
    public void deleteSeller(Long sellerId) {
        sellerRepository.deleteById(sellerId);
    }
}
