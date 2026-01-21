package com.coc.modi.member.member.presentation.dto;

import java.util.List;

public record MemberAuthzResponse(
		Long memberId,
		List<String> roles
) {
}
