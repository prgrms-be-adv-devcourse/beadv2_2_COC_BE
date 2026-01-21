package com.coc.modi.member.admin.blacklist.application;

import com.coc.modi.member.admin.blacklist.domain.BlacklistStatus;
import com.coc.modi.member.admin.blacklist.domain.MemberBlacklist;
import com.coc.modi.member.admin.blacklist.domain.MemberBlacklistRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BlacklistExpiryScheduler {

	private static final long SYSTEM_ACTOR_ID = 0L;

	private final MemberBlacklistRepository blacklistRepository;

	@Scheduled(fixedDelayString = "${blacklist.expiry-scan.delay-ms:600000}")
	@Transactional
	public void releaseExpired() {

		LocalDateTime now = LocalDateTime.now();
		List<MemberBlacklist> expired =
				blacklistRepository.findByStatusAndSuspendedUntilBefore(BlacklistStatus.SUSPENDED, now);

		if (expired.isEmpty()) {
			return;
		}

		for (MemberBlacklist blacklist : expired) {
			blacklist.release(null, now, SYSTEM_ACTOR_ID);
		}

		blacklistRepository.saveAll(expired);
	}
}
