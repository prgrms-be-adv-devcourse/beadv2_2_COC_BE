package com.coc.modi.admin.infrastructure.client.member.dto;

import java.util.List;

import org.springframework.data.domain.Pageable;

public record MemberPageRequest(
		Integer page,
		Integer size,
		List<String> sort
) {
	public static MemberPageRequest from(Pageable pageable) {

		List<String> sorts = pageable.getSort().stream()
				.map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
				.toList();

		return new MemberPageRequest(pageable.getPageNumber(), pageable.getPageSize(), sorts);
	}
}
