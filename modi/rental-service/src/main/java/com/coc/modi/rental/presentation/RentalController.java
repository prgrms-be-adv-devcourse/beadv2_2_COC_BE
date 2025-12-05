package com.coc.modi.rental.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.application.RentalCreationService;
import com.coc.modi.rental.application.RentalDecisionService;
import com.coc.modi.rental.application.RentalLifecycleService;
import com.coc.modi.rental.application.RentalPaymentService;
import com.coc.modi.rental.application.dto.PayRentalResponse;
import com.coc.modi.rental.application.dto.RentalReturnResponse;
import com.coc.modi.rental.presentation.dto.RentalFromCartRequest;
import com.coc.modi.rental.presentation.dto.RentalRequest;
import com.coc.modi.rental.presentation.dto.RentalReturnRequest;
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

    @PatchMapping("/{rentalItemId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveRentalItem(@PathVariable(name = "rentalItemId") Long rentalItemId,
                                                               @RequestParam Long memberId) {

        return rentalDecisionService.approveRentalItem(rentalItemId, memberId);
    }

    @PatchMapping("/{rentalItemId}/decline")
    public ResponseEntity<ApiResponse<Void>> declineRentalItem(@PathVariable(name = "rentalItemId") Long rentalItemId,
                                                               @RequestParam Long memberId) {

        return rentalDecisionService.declineRentalItem(rentalItemId, memberId);
    }

    @PostMapping("/{rentalId}/pay")
    public ResponseEntity<ApiResponse<PayRentalResponse>> completePayment(@PathVariable(name = "rentalId") Long rentalId,
                                                                          @RequestParam Long memberId) {

        return rentalPaymentService.completePayment(rentalId, memberId);
    }

    @PatchMapping("/{rentalId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRental(@PathVariable(name = "rentalId") Long rentalId,
                                                          @RequestParam Long memberId) {

        return rentalLifecycleService.cancelRental(rentalId, memberId);
    }

    @PostMapping("/{rentalId}/return")
    public ResponseEntity<ApiResponse<RentalReturnResponse>> completeReturn(@PathVariable(name = "rentalId") Long rentalId,
                                                                            @RequestParam Long memberId,
                                                                            @RequestBody RentalReturnRequest rentalReturnRequest) {

        return rentalLifecycleService.completeReturn(rentalReturnRequest.toCommand(rentalId, memberId));
    }

    @PostMapping("/{rentalId}/refund")
    public ResponseEntity<ApiResponse<Void>> refundRental(@PathVariable(name = "rentalId") Long rentalId,
                                                          @RequestParam Long memberId) {

        return rentalLifecycleService.refundRental(rentalId, memberId);
    }
}
