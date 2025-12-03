package com.coc.modi.member.application.dto;

import java.time.LocalDateTime;

public record MemberProfileResponse(
        String email,
        String name,
        String phone,
        LocalDateTime createdAt
) {

}
