package com.coc.modi.rental.cart.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.rental.cart.application.CartCommandService;
import com.coc.modi.rental.cart.application.CartQueryService;
import com.coc.modi.rental.cart.application.dto.CartResponse;
import com.coc.modi.rental.cart.presentation.dto.AddCartItemRequest;
import com.coc.modi.rental.cart.presentation.dto.UpdateCartItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
@Validated
public class CartController {

    private final CartCommandService cartCommandService;
    private final CartQueryService cartQueryService;

    @GetMapping()
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(@AuthenticationPrincipal CustomMember member) {
		
        return ResponseEntity.ok(ApiResponse.ok(cartQueryService.getCart(member.getMemberId())));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<Void>> addItem(@Valid @RequestBody AddCartItemRequest request,
													 @AuthenticationPrincipal CustomMember member) {

        cartCommandService.addItem(request.toCommand(member.getMemberId()));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/me/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> updateItem(@PathVariable @Positive Long cartItemId,
														@AuthenticationPrincipal CustomMember member,
                                                        @Valid @RequestBody UpdateCartItemRequest request) {
		
        cartCommandService.updateItem(request.toCommand(member.getMemberId(), cartItemId));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/me/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable @Positive Long cartItemId,
														@AuthenticationPrincipal CustomMember member) {
		
        cartCommandService.deleteItem(member.getMemberId(), cartItemId);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
