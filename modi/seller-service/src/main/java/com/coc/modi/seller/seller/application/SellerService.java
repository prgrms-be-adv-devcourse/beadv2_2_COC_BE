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
@Transactional(readOnly = true)
public class SellerService {

    private final SellerRepository sellerRepository;

    public Page<SellerInfo> findSellers(Pageable pageable) {
        return sellerRepository.findAll(pageable)
                .map(SellerInfo::from);
    }

    public SellerInfo getSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found. id=" + sellerId));
        return SellerInfo.from(seller);
    }

    @Transactional
    public SellerInfo registerSeller(SellerCreateCommand command) {
        if (sellerRepository.existsByMemberId(command.memberId())) {
            throw new IllegalArgumentException("Seller already registered for memberId=" + command.memberId());
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
                .orElseThrow(() -> new IllegalArgumentException("Seller not found. id=" + sellerId));

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
