package com.coc.modi.member.presentation;

import com.coc.modi.member.application.MemberService;
import com.coc.modi.member.application.dto.CreateMemberCommand;
import com.coc.modi.member.application.dto.MemberProfileResponse;
import com.coc.modi.member.application.dto.MemberSignupResponse;
import com.coc.modi.member.presentation.dto.MemberSignupRequest;
import com.coc.modi.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    // 회원가입
    @PostMapping
    public ResponseEntity<ApiResponse<MemberSignupResponse>> signup(@RequestBody MemberSignupRequest request){

        MemberSignupResponse response = memberService.signup(request.toCommand());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // 내 정보 조회
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getProfile(Authentication authentication){

        MemberProfileResponse response = memberService.getProfile(authentication);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

}
