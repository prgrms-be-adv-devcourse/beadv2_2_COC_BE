package com.coc.gateway.security.authz;

import java.util.List;

public record MemberAuthzResponse(
		Long memberId,
		List<String> roles
) {
}
