package com.coc.modi.rental.cart.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.cart.application.CartCommandService;
import com.coc.modi.rental.cart.application.CartQueryService;
import com.coc.modi.rental.cart.application.dto.CartResponse;
import com.coc.modi.rental.cart.presentation.dto.AddCartItemRequest;
import com.coc.modi.rental.cart.presentation.dto.UpdateCartItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(Authentication authentication) {

		Long memberId = (Long) authentication.getPrincipal();
		
        return ResponseEntity.ok(ApiResponse.ok(cartQueryService.getCart(memberId)));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<Void>> addItem(@Valid @RequestBody AddCartItemRequest request,
                                                     Authentication authentication) {
		
		Long memberId = (Long) authentication.getPrincipal();

        cartCommandService.addItem(request.toCommand(memberId));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/me/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> updateItem(@PathVariable @Positive Long cartItemId,
                                                        Authentication authentication,
                                                        @Valid @RequestBody UpdateCartItemRequest request) {

		Long memberId = (Long) authentication.getPrincipal();
		
        cartCommandService.updateItem(request.toCommand(memberId, cartItemId));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/me/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable @Positive Long cartItemId,
                                                        Authentication authentication) {

		Long memberId = (Long) authentication.getPrincipal();
		
        cartCommandService.deleteItem(memberId, cartItemId);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
