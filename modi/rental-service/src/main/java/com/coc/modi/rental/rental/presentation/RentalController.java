package com.coc.modi.rental.rental.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.rental.application.*;
import com.coc.modi.rental.rental.application.dto.PayRentalResponse;
import com.coc.modi.rental.rental.application.dto.RentalResponse;
import com.coc.modi.rental.rental.application.dto.RentalReturnResponse;
import com.coc.modi.rental.rental.domain.RentalStatus;
import com.coc.modi.rental.rental.presentation.dto.*;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

        rentalCreationService.createRentalFromCart(rentalFromCartRequest.toCommand(memberId));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createRental(@RequestBody RentalRequest rentalRequest,
                                                          @RequestParam Long memberId) {

        rentalCreationService.createRental(rentalRequest.toCommand(memberId));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/{rentalItemId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptRentalItem(@PathVariable(name = "rentalItemId") Long rentalItemId,
                                                              @RequestParam Long memberId) {

        rentalDecisionService.acceptRentalItem(rentalItemId, memberId);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/{rentalItemId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRentalItem(@PathVariable(name = "rentalItemId") Long rentalItemId,
                                                              @RequestParam Long memberId) {

        rentalDecisionService.rejectRentalItem(rentalItemId, memberId);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{rentalId}/pay")
    public ResponseEntity<ApiResponse<PayRentalResponse>> completePayment(@PathVariable(name = "rentalId") Long rentalId,
                                                                          @RequestParam Long memberId) {

        return ResponseEntity.ok(ApiResponse.ok(rentalPaymentService.completePayment(rentalId, memberId)));
    }

    @PatchMapping("/{rentalItemId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRentalItem(@PathVariable(name = "rentalItemId") Long rentalItemId,
                                                              @RequestParam Long memberId) {

        rentalLifecycleService.cancelRentalItem(rentalItemId, memberId);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{rentalItemId}/return")
    public ResponseEntity<ApiResponse<RentalReturnResponse>> completeReturn(@PathVariable(name = "rentalItemId") Long rentalItemId,
                                                                            @RequestParam Long memberId,
                                                                            @RequestBody RentalReturnRequest rentalReturnRequest) {

        return ResponseEntity.ok(ApiResponse.ok(rentalLifecycleService.completeReturn(
                rentalReturnRequest.toCommand(rentalItemId, memberId))));
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

        rentalLifecycleService.extendRentalItem(request.toCommand(rentalItemId, memberId));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{rentalId}")
    public ResponseEntity<ApiResponse<RentalResponse>> getRentalDetails(@PathVariable(name = "rentalId") Long rentalId,
                                                                        @RequestParam Long memberId) {

        return ResponseEntity.ok(ApiResponse.ok(rentalQueryService.getRentalDetails(rentalId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RentalResponse>>> searchRentals(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) RentalStatus rentalStatus) {

        return ResponseEntity.ok(ApiResponse.ok(rentalQueryService.searchRentals(startDate, endDate, rentalStatus)));
    }
}
