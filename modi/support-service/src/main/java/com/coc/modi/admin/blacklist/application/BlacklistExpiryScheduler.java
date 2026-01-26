package com.coc.modi.admin.blacklist.application;

import com.coc.modi.admin.blacklist.domain.BlacklistStatus;
import com.coc.modi.admin.blacklist.domain.MemberBlacklist;
import com.coc.modi.admin.blacklist.domain.MemberBlacklistRepository;
import com.coc.modi.admin.infrastructure.client.member.MemberAdminClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BlacklistExpiryScheduler {

	private static final long SYSTEM_ACTOR_ID = 0L;
	private static final String MEMBER_STATUS_ACTIVE = "ACTIVE";

	private final MemberBlacklistRepository blacklistRepository;
	private final MemberAdminClient memberAdminClient;

	@Scheduled(fixedDelayString = "${blacklist.expiry-scan.delay-ms:600000}")
	@Transactional
	public void releaseExpired() {

		LocalDateTime now = LocalDateTime.now();
		List<MemberBlacklist> expired =
				blacklistRepository.findByStatusAndSuspendedUntilBefore(BlacklistStatus.SUSPENDED, now);

		if (expired.isEmpty()) {
			return;
		}

		List<MemberBlacklist> released = new ArrayList<>();
		for (MemberBlacklist blacklist : expired) {
			try {
				memberAdminClient.updateMemberStatus(blacklist.getMemberId(), MEMBER_STATUS_ACTIVE);
			} catch (Exception ex) {
				log.warn("블랙리스트 만료 해제 중 회원 상태 변경 실패 memberId={}", blacklist.getMemberId(), ex);
				continue;
			}
			blacklist.release(null, now, SYSTEM_ACTOR_ID);
			released.add(blacklist);
		}

		if (!released.isEmpty()) {
			blacklistRepository.saveAll(released);
		}
	}
}
