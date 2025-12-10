package com.coc.modi.member.presentation;

import com.coc.modi.member.application.MemberService;
import com.coc.modi.member.application.dto.CreateMemberCommand;
import com.coc.modi.member.application.dto.MemberProfileResponse;
import com.coc.modi.member.application.dto.MemberSignupResponse;
import com.coc.modi.member.presentation.dto.MemberSignupRequest;
import com.coc.modi.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ApiResponse<MemberSignupResponse> signup(@RequestBody MemberSignupRequest request){

        CreateMemberCommand command = new CreateMemberCommand(
                request.email(),
                request.password(),
                request.name(),
                request.phone()
        );

        MemberSignupResponse response = memberService.signup(command);

        return ApiResponse.ok(response);
    }

    @GetMapping("/profile")
    public ApiResponse<MemberProfileResponse> getProfile(Authentication authentication){

        MemberProfileResponse response = memberService.getProfile(authentication);

        return ApiResponse.ok(response);
    }

}
