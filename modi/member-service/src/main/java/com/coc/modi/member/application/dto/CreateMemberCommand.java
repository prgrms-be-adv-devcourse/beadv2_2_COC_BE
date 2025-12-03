package com.coc.modi.member.application.dto;

public record CreateMemberCommand (
        String email,
        String password,
        String name,
        String phone
) {
}
