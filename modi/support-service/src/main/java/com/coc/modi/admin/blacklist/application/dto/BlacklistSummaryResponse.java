package com.coc.modi.admin.blacklist.application.dto;

import com.coc.modi.admin.blacklist.domain.BlacklistStatus;
import com.coc.modi.admin.blacklist.domain.MemberBlacklist;
import com.coc.modi.admin.infrastructure.client.member.dto.MemberSummaryResponse;

import java.time.LocalDateTime;

public record BlacklistSummaryResponse(
		Long memberId,
		String email,
		String name,
		BlacklistStatus status,
		LocalDateTime suspendedAt,
		LocalDateTime suspendedUntil,
		LocalDateTime releasedAt
) {

	public static BlacklistSummaryResponse of(MemberSummaryResponse member, MemberBlacklist blacklist) {

		if (blacklist == null) {
			return new BlacklistSummaryResponse(
					member.memberId(),
					member.email(),
					member.name(),
					BlacklistStatus.ACTIVE,
					null,
					null,
					null
			);
		}

		return new BlacklistSummaryResponse(
				member.memberId(),
				member.email(),
				member.name(),
				blacklist.getStatus(),
				blacklist.getSuspendedAt(),
				blacklist.getSuspendedUntil(),
				blacklist.getReleasedAt()
		);
	}
}
