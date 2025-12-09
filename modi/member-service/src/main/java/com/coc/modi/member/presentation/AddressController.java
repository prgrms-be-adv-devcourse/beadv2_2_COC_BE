package com.coc.modi.member.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.member.application.AddressService;
import com.coc.modi.member.application.dto.AddressListResponse;
import com.coc.modi.member.presentation.dto.AddressCreateRequest;
import com.coc.modi.member.presentation.dto.AddressUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

	// 주소 목록 조회
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AddressListResponse>> getProfileAddresses(Authentication authentication) {

        AddressListResponse response = addressService.getProfileAddresses(authentication);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

	// 주소 등록
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<?>> createAddress(Authentication authentication,
                                                        @RequestBody AddressCreateRequest request) {

        Long memberId = (Long)authentication.getPrincipal();
		
        addressService.createAddress(request.toCommand(memberId));

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

	// 주소 수정
    @PutMapping("/profile/{addressId}")
    public ResponseEntity<ApiResponse<?>> updateAddress(Authentication authentication,
                                                        @PathVariable Long addressId,
                                                        @RequestBody AddressUpdateRequest request) {

        Long memberId = (Long)authentication.getPrincipal();
		
        addressService.updateAddress(request.toCommand(memberId, addressId));

        return ResponseEntity.ok(ApiResponse.ok());
    }

	// 주소 삭제
    @DeleteMapping("/profile/{addressId}")
    public ResponseEntity<ApiResponse<?>> deleteAddress(Authentication authentication,
                                                        @PathVariable Long addressId) {

        Long memberId = (Long)authentication.getPrincipal();
		
        addressService.deleteAddress(memberId, addressId);

        return ResponseEntity.ok(ApiResponse.ok());
    }
}
