package com.coc.modi.member.member.application.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import com.coc.modi.member.member.domain.Member;

public record MemberPageResponse(
		List<MemberSummaryResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean last
) {
	public static MemberPageResponse from(Page<Member> members) {

		List<MemberSummaryResponse> content = members.getContent().stream()
				.map(MemberSummaryResponse::from)
				.toList();

		return new MemberPageResponse(
				content,
				members.getNumber(),
				members.getSize(),
				members.getTotalElements(),
				members.getTotalPages(),
				members.isLast()
		);
	}
}
