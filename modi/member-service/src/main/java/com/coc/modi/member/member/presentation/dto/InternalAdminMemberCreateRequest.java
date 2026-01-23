package com.coc.modi.member.member.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.coc.modi.member.member.application.dto.InternalAdminMemberCreateCommand;

public record InternalAdminMemberCreateRequest(
		@NotBlank String email,
		@NotBlank String password,
		@NotBlank String name,
		@NotBlank String phone,
		@NotNull Long createdBy
) {
	public InternalAdminMemberCreateCommand toCommand() {

		return new InternalAdminMemberCreateCommand(email, password, name, phone, createdBy);
	}
}
