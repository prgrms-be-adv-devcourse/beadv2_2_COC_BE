package com.coc.modi.rental.rental.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.rental.application.*;
import com.coc.modi.rental.rental.application.dto.PayRentalResponse;
import com.coc.modi.rental.rental.application.dto.RentalResponse;
import com.coc.modi.rental.rental.application.dto.RentalReturnResponse;
import com.coc.modi.rental.rental.presentation.dto.RentalFromCartRequest;
import com.coc.modi.rental.rental.presentation.dto.RentalRequest;
import com.coc.modi.rental.rental.presentation.dto.RentalReturnRequest;
import com.coc.modi.rental.rental.presentation.dto.ExtendRentalRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalCreationService rentalCreationService;
    private final RentalDecisionService rentalDecisionService;
    private final RentalLifecycleService rentalLifecycleService;
    private final RentalPaymentService rentalPaymentService;
    private final RentalQueryService rentalQueryService;

    @PostMapping("/carts")
    public ResponseEntity<ApiResponse<Void>> createRentalFromCart(@RequestBody RentalFromCartRequest rentalFromCartRequest,
                                                                  @RequestParam Long memberId) {
        //현재 @RequestParam으로 받는 멤버 ID 값을 멤버 서비스 적용 이후에는 AuthenticationPrincipal에서 가져와 사용할 예정

        return rentalCreationService.createRentalFromCart(rentalFromCartRequest.toCommand(memberId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createRental(@RequestBody RentalRequest rentalRequest,
                                                          @RequestParam Long memberId) {

        return rentalCreationService.createRental(rentalRequest.toCommand(memberId));
    }

    @PatchMapping("/{rentalItemId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptRentalItem(@PathVariable(name = "rentalItemId") Long rentalItemId,
                                                              @RequestParam Long memberId) {

        return rentalDecisionService.acceptRentalItem(rentalItemId, memberId);
    }

    @PatchMapping("/{rentalItemId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRentalItem(@PathVariable(name = "rentalItemId") Long rentalItemId,
                                                              @RequestParam Long memberId) {

        return rentalDecisionService.rejectRentalItem(rentalItemId, memberId);
    }

    @PostMapping("/{rentalId}/pay")
    public ResponseEntity<ApiResponse<PayRentalResponse>> completePayment(@PathVariable(name = "rentalId") Long rentalId,
                                                                          @RequestParam Long memberId) {

        return rentalPaymentService.completePayment(rentalId, memberId);
    }

    @PatchMapping("/{rentalItemId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRentalItem(@PathVariable(name = "rentalItemId") Long rentalItemId,
                                                          @RequestParam Long memberId) {

        return rentalLifecycleService.cancelRentalItem(rentalItemId, memberId);
    }

    @PostMapping("/{rentalItemId}/return")
    public ResponseEntity<ApiResponse<RentalReturnResponse>> completeReturn(@PathVariable(name = "rentalItemId") Long rentalItemId,
                                                                            @RequestParam Long memberId,
                                                                            @RequestBody RentalReturnRequest rentalReturnRequest) {

        return rentalLifecycleService.completeReturn(rentalReturnRequest.toCommand(rentalItemId, memberId));
    }

    @PostMapping("/{rentalItemId}/refund")
    public ResponseEntity<ApiResponse<Void>> refundRental(@PathVariable(name = "rentalItemId") Long rentalItemId,
                                                          @RequestParam Long memberId) {

        return rentalLifecycleService.refundRentalItem(rentalItemId, memberId);
    }

    @PostMapping("/{rentalItemId}/extend")
    public ResponseEntity<ApiResponse<Void>> extendRental(@PathVariable(name = "rentalItemId") Long rentalItemId,
                                                          @RequestParam Long memberId,
                                                          @RequestBody ExtendRentalRequest request) {

        return rentalLifecycleService.extendRentalItem(request.toCommand(rentalItemId, memberId));
    }

//    @GetMapping
//    public ResponseEntity<ApiResponse<List<RentalResponse>>> getRentalList(@RequestParam Long memberId, 여기에 동적 쿼리 필요
//                                                                         @RequestBody) {}

    @GetMapping("/{rentalId}")
    public ResponseEntity<ApiResponse<RentalResponse>> getRentalDetails(@PathVariable(name = "rentalId") Long rentalId,
                                                                        @RequestParam Long memberId) {

        return rentalQueryService.getRentalDetails(rentalId);
    }
}
