package com.coc.gateway.security.authz;

public record AuthzRequestContext(
		String method,
		String path
) {
}
