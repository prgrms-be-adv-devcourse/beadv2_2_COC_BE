package com.coc.modi.member.member.presentation;

import java.util.List;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.member.member.application.AddressService;
import com.coc.modi.member.member.application.dto.AddressResponse;
import com.coc.modi.member.member.presentation.dto.AddressCreateRequest;
import com.coc.modi.member.member.presentation.dto.AddressUpdateRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

	// 주소 목록 조회
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getProfileAddresses(@AuthenticationPrincipal CustomMember member) {
		
        return ResponseEntity.ok(ApiResponse.ok(addressService.getProfileAddresses(member.getMemberId())));
    }

	// 주소 등록
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> createAddress(@AuthenticationPrincipal CustomMember member,
														   @Valid @RequestBody AddressCreateRequest request) {
		
        addressService.createAddress(request.toCommand(member.getMemberId()));

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(null));
    }

	// 주소 수정
    @PutMapping("/profile/{addressId}")
    public ResponseEntity<ApiResponse<Void>> updateAddress(@AuthenticationPrincipal CustomMember member,
                                                           @PathVariable Long addressId,
                                                           @Valid @RequestBody AddressUpdateRequest request) {
		
        addressService.updateAddress(request.toCommand(member.getMemberId(), addressId));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

	// 주소 삭제
    @DeleteMapping("/profile/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@AuthenticationPrincipal CustomMember member,
                                                           @PathVariable Long addressId) {
		
        addressService.deleteAddress(member.getMemberId(), addressId);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
