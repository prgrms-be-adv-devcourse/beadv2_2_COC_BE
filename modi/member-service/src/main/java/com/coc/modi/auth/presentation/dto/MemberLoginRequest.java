package com.coc.modi.auth.presentation.dto;

import com.coc.modi.auth.application.dto.MemberLoginCommand;

public record MemberLoginRequest (
        String email,
        String password
) {
    public MemberLoginCommand toCommand(){

        return new MemberLoginCommand(
                email,
                password
        );
    }
}
