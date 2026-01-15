package com.coc.gateway.security.permission;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EndpointPermission {

	private Long id;

	private String method;

	private String pathPattern;

	private String roles;
}
