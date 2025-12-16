package com.coc.modi.member.auth.application.dto;

import org.springframework.http.ResponseCookie;

public record LogoutResponse(
		ResponseCookie clear
) {
}
