package com.coc.modi.member.admin.blacklist.application.dto;

import com.coc.modi.member.admin.blacklist.domain.BlacklistStatus;
import com.coc.modi.member.admin.blacklist.domain.MemberBlacklist;
import com.coc.modi.member.member.domain.Member;

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

	public static BlacklistSummaryResponse of(Member member, MemberBlacklist blacklist) {

		if (blacklist == null) {
			return new BlacklistSummaryResponse(
					member.getId(),
					member.getEmail(),
					member.getName(),
					BlacklistStatus.ACTIVE,
					null,
					null,
					null
			);
		}

		return new BlacklistSummaryResponse(
				member.getId(),
				member.getEmail(),
				member.getName(),
				blacklist.getStatus(),
				blacklist.getSuspendedAt(),
				blacklist.getSuspendedUntil(),
				blacklist.getReleasedAt()
		);
	}
}
