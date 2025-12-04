package com.coc.modi.auth.application.dto;

public record MemberLoginResponse (
        String accessToken,
        String refreshToken,
        MemberData member
){
    public record MemberData (
            Long id,
            String email,
            String name,
            String role
    ){}
}
