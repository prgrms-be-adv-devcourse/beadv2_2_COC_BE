package com.coc.modi.seller.seller.application;

import com.coc.modi.seller.application.SellerRentalService;
import com.coc.modi.seller.application.dto.SellerRentalResponse;
import com.coc.modi.seller.exception.SellerDuplicateException;
import com.coc.modi.seller.exception.SellerNotFoundException;
import com.coc.modi.seller.seller.application.dto.SellerCreateCommand;
import com.coc.modi.seller.seller.application.dto.SellerDetailResponse;
import com.coc.modi.seller.seller.application.dto.SellerUpdateCommand;
import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final SellerRentalService sellerRentalService;

    @Transactional(readOnly = true)
    public SellerDetailResponse getSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다. id=" + sellerId));
        return SellerDetailResponse.from(seller);
    }

    @Transactional(readOnly = true)
    public SellerDetailResponse getSellerByMemberId(Long memberId) {
        Seller seller = sellerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다. memberId=" + memberId));
        return SellerDetailResponse.from(seller);
    }

    @Transactional
    public SellerDetailResponse registerSeller(SellerCreateCommand command) {
        if (sellerRepository.existsByMemberId(command.memberId())) {
            throw new SellerDuplicateException("이미 등록된 판매자입니다. memberId=" + command.memberId());
        }

        Seller seller = Seller.create(
                command.memberId(),
                command.storeName(),
                command.bizRegNo(),
                command.storePhone()
        );

        Seller saved = sellerRepository.save(seller);
        return SellerDetailResponse.from(saved);
    }


    @Transactional
    public SellerDetailResponse updateSellerByMemberId(Long memberId, SellerUpdateCommand command) {
        Seller seller = sellerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다. memberId=" + memberId));

        seller.update(
                command.storeName(),
                command.bizRegNo(),
                command.storePhone()
        );
        seller.changeStatus(command.status());

        return SellerDetailResponse.from(seller);
    }
    

    @Transactional(readOnly = true)
    public List<SellerRentalResponse> getMyRentals(Long memberId,
                                                   Long productId,
                                                   String status,
                                                   String startDate,
                                                   String endDate,
                                                   Integer page,
                                                   Integer size) {
        Seller seller = sellerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다. memberId=" + memberId));
        return sellerRentalService.getSellerRentals(
                seller.getId(),
                productId,
                status,
                startDate,
                endDate,
                page,
                size
        );
    }
}
