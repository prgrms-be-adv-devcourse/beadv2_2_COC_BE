package com.coc.modi.auth.application.dto;

import com.coc.modi.auth.presentation.dto.MemberLoginInfo;

public record MemberLoginResponse (
        String accessToken,
        String refreshToken,
        MemberLoginInfo member
){
}
