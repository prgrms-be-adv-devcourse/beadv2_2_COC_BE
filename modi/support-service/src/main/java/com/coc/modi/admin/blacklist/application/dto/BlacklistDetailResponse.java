package com.coc.modi.admin.blacklist.application.dto;

import com.coc.modi.admin.blacklist.domain.BlacklistStatus;
import com.coc.modi.admin.blacklist.domain.MemberBlacklist;
import com.coc.modi.admin.infrastructure.client.member.dto.MemberSummaryResponse;

import java.time.LocalDateTime;

public record BlacklistDetailResponse(
		Long memberId,
		String email,
		String name,
		String phone,
		BlacklistStatus status,
		String reason,
		String memo,
		LocalDateTime suspendedAt,
		LocalDateTime suspendedUntil,
		LocalDateTime releasedAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static BlacklistDetailResponse of(MemberSummaryResponse member, MemberBlacklist blacklist) {

		if (blacklist == null) {
			return new BlacklistDetailResponse(
					member.memberId(),
					member.email(),
					member.name(),
					member.phone(),
					BlacklistStatus.ACTIVE,
					null,
					null,
					null,
					null,
					null,
					member.createdAt(),
					member.updatedAt()
			);
		}

		return new BlacklistDetailResponse(
				member.memberId(),
				member.email(),
				member.name(),
				member.phone(),
				blacklist.getStatus(),
				blacklist.getReason(),
				blacklist.getMemo(),
				blacklist.getSuspendedAt(),
				blacklist.getSuspendedUntil(),
				blacklist.getReleasedAt(),
				blacklist.getCreatedAt(),
				blacklist.getUpdatedAt()
		);
	}
}
