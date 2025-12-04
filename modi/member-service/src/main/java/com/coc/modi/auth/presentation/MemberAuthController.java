package com.coc.modi.auth.presentation;

import com.coc.modi.auth.application.MemberAuthService;
import com.coc.modi.auth.application.dto.MemberLoginCommand;
import com.coc.modi.auth.application.dto.MemberLoginResponse;
import com.coc.modi.auth.presentation.dto.MemberLoginRequest;
import com.coc.modi.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class MemberAuthController {

    private final MemberAuthService memberAuthService;

    @PostMapping("/login")
    public ApiResponse<MemberLoginResponse> login(@RequestBody MemberLoginRequest request){

        MemberLoginCommand command = new MemberLoginCommand(
                request.email(),
                request.password()
        );

        MemberLoginResponse response = memberAuthService.login(command);

        return ApiResponse.ok(response);
    }
}
