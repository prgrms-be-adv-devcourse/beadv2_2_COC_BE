package com.coc.modi.auth.presentation.dto;

public record MemberLoginRequest (
        String email,
        String password
) {
}
