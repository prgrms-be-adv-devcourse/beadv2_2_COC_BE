package com.coc.modi.member.admin.blacklist.application.dto;

import com.coc.modi.member.admin.blacklist.domain.BlacklistStatus;
import com.coc.modi.member.admin.blacklist.domain.MemberBlacklist;
import com.coc.modi.member.member.domain.Member;

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

	public static BlacklistDetailResponse of(Member member, MemberBlacklist blacklist) {

		if (blacklist == null) {
			return new BlacklistDetailResponse(
					member.getId(),
					member.getEmail(),
					member.getName(),
					member.getPhone(),
					BlacklistStatus.ACTIVE,
					null,
					null,
					null,
					null,
					null,
					member.getCreatedAt(),
					member.getUpdatedAt()
			);
		}

		return new BlacklistDetailResponse(
				member.getId(),
				member.getEmail(),
				member.getName(),
				member.getPhone(),
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
