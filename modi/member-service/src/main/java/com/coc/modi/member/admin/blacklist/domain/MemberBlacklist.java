package com.coc.modi.member.admin.blacklist.domain;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "member_blacklist", schema = "admin")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberBlacklist extends BaseEntity {

	@Id
	@Column(name = "member_id")
	private Long memberId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BlacklistStatus status;

	@Column(nullable = false, length = 500)
	private String reason;

	@Column(length = 1000)
	private String memo;

	@Column(name = "suspended_at")
	private LocalDateTime suspendedAt;

	@Column(name = "suspended_until")
	private LocalDateTime suspendedUntil;

	@Column(name = "released_at")
	private LocalDateTime releasedAt;

	@Column(name = "created_by", nullable = false)
	private Long createdBy;

	@Column(name = "updated_by", nullable = false)
	private Long updatedBy;

	private MemberBlacklist(Long memberId,
							BlacklistStatus status,
							String reason,
							String memo,
							LocalDateTime suspendedAt,
							LocalDateTime suspendedUntil,
							LocalDateTime releasedAt,
							Long createdBy,
							Long updatedBy) {

		this.memberId = memberId;
		this.status = status;
		this.reason = reason;
		this.memo = memo;
		this.suspendedAt = suspendedAt;
		this.suspendedUntil = suspendedUntil;
		this.releasedAt = releasedAt;
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
	}

	public static MemberBlacklist suspend(Long memberId,
										  String reason,
										  String memo,
										  LocalDateTime suspendedAt,
										  LocalDateTime suspendedUntil,
										  Long actorId) {

		return new MemberBlacklist(
				memberId,
				BlacklistStatus.SUSPENDED,
				reason,
				memo,
				suspendedAt,
				suspendedUntil,
				null,
				actorId,
				actorId
		);
	}

	public void updateSuspension(String reason,
								 String memo,
								 LocalDateTime suspendedAt,
								 LocalDateTime suspendedUntil,
								 Long actorId) {

		this.status = BlacklistStatus.SUSPENDED;
		this.reason = reason;
		this.memo = memo;
		this.suspendedAt = suspendedAt;
		this.suspendedUntil = suspendedUntil;
		this.releasedAt = null;
		this.updatedBy = actorId;
	}

	public void release(String memo, LocalDateTime releasedAt, Long actorId) {

		this.status = BlacklistStatus.ACTIVE;
		if (memo != null) {
			this.memo = memo;
		}
		this.releasedAt = releasedAt;
		this.suspendedAt = null;
		this.suspendedUntil = null;
		this.updatedBy = actorId;
	}

	public boolean isExpired(LocalDateTime now) {

		return status == BlacklistStatus.SUSPENDED
				&& suspendedUntil != null
				&& now.isAfter(suspendedUntil);
	}
}
