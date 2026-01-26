package com.coc.modi.seller.seller.presentation.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.seller.seller.application.SellerApprovalService;
import com.coc.modi.seller.seller.application.SellerService;
import com.coc.modi.seller.seller.application.dto.SellerDetailResponse;
import com.coc.modi.seller.seller.application.dto.SellerIdResponse;
import com.coc.modi.seller.seller.application.dto.SellerRegistrationPageResponse;
import com.coc.modi.seller.seller.application.dto.SellerRegistrationResponse;
import com.coc.modi.seller.seller.registration.domain.SellerRegistrationStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/sellers")
public class SellerInternalController {

    private final SellerService sellerService;
    private final SellerApprovalService sellerApprovalService;

    @GetMapping("/by-member/{memberId}")
    public SellerIdResponse getSellerInfo(@PathVariable Long memberId) {

        SellerDetailResponse seller = sellerService.getSellerByMemberId(memberId);

        return new SellerIdResponse(seller.sellerId(), seller.memberId());
    }

    @GetMapping("/{sellerId}")
    public SellerDetailResponse getSellerById(@PathVariable Long sellerId) {

        return sellerService.getSeller(sellerId);
    }
}
