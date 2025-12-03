package com.coc.modi.rental.presentation;

import com.coc.modi.rental.application.RentalService;
import com.coc.modi.rental.application.dto.CreateRentalFromCartCommand;
import com.coc.modi.rental.application.dto.RentalDetailResponse;
import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.presentation.dto.RentalFromCartRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;

//    @PostMapping("/carts")
//    public ResponseEntity<ApiResponse<RentalDetailResponse>> createRentalFromCart(@RequestBody @Valid RentalFromCartRequest request,
//                                                                                  @RequestParam Long memberId) {
//        //현재 @RequestParam으로 받는 멤버 ID 값을 멤버 서비스 적용 이후에는 AuthenticationPrincipal에서 가져와 사용할 예정
//
//        return ResponseEntity.status(201).body(ApiResponse.ok(rentalService.createRentalFromCart(request.toCommand(memberId))));
//    }
}
