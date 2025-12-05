package com.coc.modi.member.application.dto;

import com.coc.modi.member.domain.Member;

import java.time.LocalDateTime;

public record MemberSignupResponse(
        String email,
        String name,
        String phone,
        LocalDateTime createdAt
) {
    public static MemberSignupResponse from(Member member) {

        return new MemberSignupResponse(
                member.getEmail(),
                member.getName(),
                member.getPhone(),
                member.getCreatedAt()
        );
    }
}
