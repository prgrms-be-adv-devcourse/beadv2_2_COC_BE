package com.coc.modi.rental.cart.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.cart.application.CartCommandService;
import com.coc.modi.rental.cart.application.CartQueryService;
import com.coc.modi.rental.cart.application.dto.CartResponse;
import com.coc.modi.rental.cart.presentation.dto.AddCartItemRequest;
import com.coc.modi.rental.cart.presentation.dto.UpdateCartItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

    private final CartCommandService cartCommandService;
    private final CartQueryService cartQueryService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(@RequestParam Long memberId) {

        return ResponseEntity.ok(ApiResponse.ok(cartQueryService.getCart(memberId)));
    }

    @PostMapping("/me/items")
    public ResponseEntity<ApiResponse<Void>> addItem(@RequestBody AddCartItemRequest request,
                                                     @RequestParam Long memberId) {

        cartCommandService.addItem(request.toCommand(memberId));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/me/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> updateItem(@PathVariable Long cartItemId,
                                                        @RequestParam Long memberId,
                                                        @RequestBody UpdateCartItemRequest request) {

        cartCommandService.updateItem(request.toCommand(memberId, cartItemId));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/me/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long cartItemId,
                                                        @RequestParam Long memberId) {

        cartCommandService.deleteItem(memberId, cartItemId);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
