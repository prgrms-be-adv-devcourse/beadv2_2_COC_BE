package com.coc.modi.member.application.dto;

import com.coc.modi.member.domain.Member;

import java.time.LocalDateTime;

public record MemberProfileResponse(
        String email,
        String name,
        String phone,
        LocalDateTime createdAt
) {
    public static MemberProfileResponse from(Member member) {

        return new MemberProfileResponse(
                member.getEmail(),
                member.getName(),
                member.getPhone(),
                member.getCreatedAt()
        );
    }
}
