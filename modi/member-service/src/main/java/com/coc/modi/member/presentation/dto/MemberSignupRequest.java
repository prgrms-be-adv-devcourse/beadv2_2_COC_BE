package com.coc.modi.member.presentation.dto;

import com.coc.modi.member.application.dto.CreateMemberCommand;

public record MemberSignupRequest (
        String email,
        String password,
        String name,
        String phone
) {
    public CreateMemberCommand toCommand(){

        return new CreateMemberCommand(
                email,
                password,
                name,
                phone
        );
    }
}
