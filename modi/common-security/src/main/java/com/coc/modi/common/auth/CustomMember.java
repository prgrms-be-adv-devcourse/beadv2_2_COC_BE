package com.coc.modi.common.auth;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

@Getter
public class CustomMember implements UserDetails {
	
	private final Long memberId;
	private final String role;
	
	public CustomMember(Long memberId,
					  String role) {
		
		this.memberId = memberId;
		this.role = role;
	}
	
	public Long getMemberId() {
		
		return memberId;
	}
	
	public String getRole() {
		
		return role;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		return List.of();
	}
	
	@Override
	public String getPassword() {
		
		return null;
	}
	
	@Override
	public String getUsername() {
		
		return memberId.toString();
	}
}
