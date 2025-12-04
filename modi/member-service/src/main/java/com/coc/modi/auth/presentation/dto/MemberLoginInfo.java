package com.coc.modi.auth.presentation.dto;

public record MemberLoginInfo(
        Long id,
        String email,
        String name,
        String role
) {
}
