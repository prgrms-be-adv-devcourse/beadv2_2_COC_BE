package com.coc.modi.common.auth;


public record CustomMember(
		Long memberId,
		String role)
{
	public boolean isSeller() {
		
		return "SELLER".equals(role);
	}
}
