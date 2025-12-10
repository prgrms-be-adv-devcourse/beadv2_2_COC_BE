package com.coc.modi.auth.application.dto;

import com.coc.modi.member.domain.Member;

public record MemberLoginResponse(
        String accessToken,
        String refreshToken,
        MemberData member
) {
    public static MemberLoginResponse of(
            String accessToken,
            String refreshToken,
            Member member
    ) {

        return new MemberLoginResponse(
                accessToken,
                refreshToken,
                MemberData.from(member)
        );
    }

    public record MemberData(
            Long id,
            String email,
            String name,
            String role
    ) {
        public static MemberData from(Member member) {

            return new MemberData(
                    member.getId(),
                    member.getEmail(),
                    member.getName(),
                    member.getRole().name()
            );
        }
    }
}
