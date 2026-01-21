package com.coc.modi.member.admin.blacklist.application;

import com.coc.modi.member.admin.blacklist.application.dto.BlacklistDetailResponse;
import com.coc.modi.member.admin.blacklist.application.dto.BlacklistReleaseCommand;
import com.coc.modi.member.admin.blacklist.application.dto.BlacklistSummaryResponse;
import com.coc.modi.member.admin.blacklist.application.dto.BlacklistSuspendCommand;
import com.coc.modi.member.admin.blacklist.domain.BlacklistStatus;
import com.coc.modi.member.admin.blacklist.domain.MemberBlacklist;
import com.coc.modi.member.admin.blacklist.domain.MemberBlacklistRepository;
import com.coc.modi.member.admin.blacklist.exception.BlacklistNotFoundException;
import com.coc.modi.member.admin.blacklist.infrastructure.MemberBlacklistQueryRepository;
import com.coc.modi.member.member.domain.Member;
import com.coc.modi.member.member.domain.MemberRepository;
import com.coc.modi.member.member.exception.MemberNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlacklistService {

	private final MemberRepository memberRepository;
	private final MemberBlacklistRepository blacklistRepository;
	private final MemberBlacklistQueryRepository blacklistQueryRepository;

	@Value("${blacklist.default-days:7}")
	private long defaultSuspendDays;

	@Transactional
	public Page<BlacklistSummaryResponse> getBlacklists(BlacklistStatus status, Pageable pageable) {

		Page<Member> members = (status == null)
				? memberRepository.findAll(pageable)
				: blacklistQueryRepository.findMembersByBlacklistStatus(status, pageable);

		List<Member> content = members.getContent();
		Map<Long, MemberBlacklist> blacklists = loadBlacklists(content);

		List<BlacklistSummaryResponse> responses = content.stream()
				.map(member -> BlacklistSummaryResponse.of(member, blacklists.get(member.getId())))
				.toList();

		return new PageImpl<>(responses, pageable, members.getTotalElements());
	}

	@Transactional
	public BlacklistSummaryResponse searchByEmail(String email) {

		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("email은 필수입니다.");
		}

		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new MemberNotFoundException(email));

		MemberBlacklist blacklist = blacklistRepository.findById(member.getId()).orElse(null);
		return BlacklistSummaryResponse.of(member, blacklist);
	}

	@Transactional
	public BlacklistDetailResponse getBlacklistDetail(Long memberId) {

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new MemberNotFoundException(memberId));

		MemberBlacklist blacklist = blacklistRepository.findById(memberId).orElse(null);
		return BlacklistDetailResponse.of(member, blacklist);
	}

	@Transactional
	public BlacklistDetailResponse suspend(BlacklistSuspendCommand command) {

		validateSuspendCommand(command);

		Member member = memberRepository.findById(command.memberId())
				.orElseThrow(() -> new MemberNotFoundException(command.memberId()));

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime suspendedUntil = now.plusDays(defaultSuspendDays);

		Optional<MemberBlacklist> optionalBlacklist = blacklistRepository.findById(command.memberId());

		MemberBlacklist blacklist = optionalBlacklist.orElseGet(() -> MemberBlacklist.suspend(
						command.memberId(),
						command.reason(),
						command.memo(),
						now,
						suspendedUntil,
						command.createdBy()
				));

		if (optionalBlacklist.isPresent()) {
			blacklist.updateSuspension(command.reason(), command.memo(), now, suspendedUntil, command.createdBy());
		}

		MemberBlacklist saved = blacklistRepository.save(blacklist);
		return BlacklistDetailResponse.of(member, saved);
	}

	@Transactional
	public BlacklistDetailResponse release(BlacklistReleaseCommand command) {

		validateReleaseCommand(command);

		Member member = memberRepository.findById(command.memberId())
				.orElseThrow(() -> new MemberNotFoundException(command.memberId()));

		MemberBlacklist blacklist = blacklistRepository.findById(command.memberId())
				.orElseThrow(() -> new BlacklistNotFoundException(command.memberId()));

		blacklist.release(command.memo(), LocalDateTime.now(), command.releasedBy());
		MemberBlacklist saved = blacklistRepository.save(blacklist);
		return BlacklistDetailResponse.of(member, saved);
	}

	private Map<Long, MemberBlacklist> loadBlacklists(List<Member> members) {

		List<Long> memberIds = members.stream()
				.map(Member::getId)
				.toList();

		if (memberIds.isEmpty()) {
			return Map.of();
		}

		List<MemberBlacklist> blacklists = blacklistRepository.findByMemberIdIn(memberIds);
		return blacklists.stream()
				.collect(Collectors.toMap(MemberBlacklist::getMemberId, blacklist -> blacklist));
	}

	private void validateSuspendCommand(BlacklistSuspendCommand command) {

		if (command == null) {
			throw new IllegalArgumentException("요청 본문이 비어 있습니다.");
		}
		if (command.memberId() == null) {
			throw new IllegalArgumentException("memberId는 필수입니다.");
		}
		if (command.reason() == null || command.reason().isBlank()) {
			throw new IllegalArgumentException("정지 사유는 필수입니다.");
		}
		if (command.createdBy() == null) {
			throw new IllegalArgumentException("createdBy는 필수입니다.");
		}
	}

	private void validateReleaseCommand(BlacklistReleaseCommand command) {

		if (command == null) {
			throw new IllegalArgumentException("요청 본문이 비어 있습니다.");
		}
		if (command.memberId() == null) {
			throw new IllegalArgumentException("memberId는 필수입니다.");
		}
		if (command.releasedBy() == null) {
			throw new IllegalArgumentException("releasedBy는 필수입니다.");
		}
	}
}
