package com.coc.modi.admin.blacklist.application;

import com.coc.modi.admin.blacklist.application.dto.BlacklistDetailResponse;
import com.coc.modi.admin.blacklist.application.dto.BlacklistReleaseCommand;
import com.coc.modi.admin.blacklist.application.dto.BlacklistSummaryResponse;
import com.coc.modi.admin.blacklist.application.dto.BlacklistSuspendCommand;
import com.coc.modi.admin.blacklist.domain.BlacklistStatus;
import com.coc.modi.admin.blacklist.domain.MemberBlacklist;
import com.coc.modi.admin.blacklist.domain.MemberBlacklistRepository;
import com.coc.modi.admin.blacklist.exception.BlacklistNotFoundException;
import com.coc.modi.admin.exception.AdminMemberNotFoundException;
import com.coc.modi.admin.infrastructure.client.member.MemberAdminClient;
import com.coc.modi.admin.infrastructure.client.member.dto.MemberPageRequest;
import com.coc.modi.admin.infrastructure.client.member.dto.MemberPageResponse;
import com.coc.modi.admin.infrastructure.client.member.dto.MemberSummaryResponse;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlacklistService {

	private static final String MEMBER_STATUS_INACTIVE = "INACTIVE";
	private static final String MEMBER_STATUS_ACTIVE = "ACTIVE";

	private final MemberAdminClient memberAdminClient;
	private final MemberBlacklistRepository blacklistRepository;

	@Value("${blacklist.default-days:7}")
	private long defaultSuspendDays;

	@Transactional
	public Page<BlacklistSummaryResponse> getBlacklists(BlacklistStatus status, Pageable pageable) {

		if (status == BlacklistStatus.SUSPENDED) {
			return getSuspendedBlacklists(pageable);
		}
		if (status == BlacklistStatus.ACTIVE) {
			return getActiveBlacklists(pageable);
		}

		return getAllBlacklists(pageable);
	}

	@Transactional
	public BlacklistSummaryResponse searchByEmail(String email) {

		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("email은 필수입니다.");
		}

		MemberSummaryResponse member = getMemberByEmail(email);
		MemberBlacklist blacklist = blacklistRepository.findById(member.memberId()).orElse(null);
		return BlacklistSummaryResponse.of(member, blacklist);
	}

	@Transactional
	public BlacklistDetailResponse getBlacklistDetail(Long memberId) {

		MemberSummaryResponse member = getMemberById(memberId);
		MemberBlacklist blacklist = blacklistRepository.findById(memberId).orElse(null);
		return BlacklistDetailResponse.of(member, blacklist);
	}

	@Transactional
	public BlacklistDetailResponse suspend(BlacklistSuspendCommand command) {

		validateSuspendCommand(command);

		MemberSummaryResponse member = getMemberById(command.memberId());

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
		memberAdminClient.updateMemberStatus(command.memberId(), MEMBER_STATUS_INACTIVE);
		return BlacklistDetailResponse.of(member, saved);
	}

	@Transactional
	public BlacklistDetailResponse release(BlacklistReleaseCommand command) {

		validateReleaseCommand(command);

		MemberSummaryResponse member = getMemberById(command.memberId());

		MemberBlacklist blacklist = blacklistRepository.findById(command.memberId())
				.orElseThrow(() -> new BlacklistNotFoundException(command.memberId()));

		blacklist.release(command.memo(), LocalDateTime.now(), command.releasedBy());
		MemberBlacklist saved = blacklistRepository.save(blacklist);
		memberAdminClient.updateMemberStatus(command.memberId(), MEMBER_STATUS_ACTIVE);
		return BlacklistDetailResponse.of(member, saved);
	}

	private Page<BlacklistSummaryResponse> getAllBlacklists(Pageable pageable) {

		MemberPageResponse members = memberAdminClient.getMembers(MemberPageRequest.from(pageable));
		List<MemberSummaryResponse> content = members.content();
		Map<Long, MemberBlacklist> blacklists = loadBlacklists(content);

		List<BlacklistSummaryResponse> responses = content.stream()
				.map(member -> BlacklistSummaryResponse.of(member, blacklists.get(member.memberId())))
				.toList();

		return new PageImpl<>(responses, pageable, members.totalElements());
	}

	private Page<BlacklistSummaryResponse> getSuspendedBlacklists(Pageable pageable) {

		Page<MemberBlacklist> blacklists = blacklistRepository.findByStatus(BlacklistStatus.SUSPENDED, pageable);
		List<Long> memberIds = blacklists.getContent().stream()
				.map(MemberBlacklist::getMemberId)
				.toList();

		Map<Long, MemberSummaryResponse> members = loadMembersByIds(memberIds);
		List<BlacklistSummaryResponse> responses = blacklists.getContent().stream()
				.map(blacklist -> {
					MemberSummaryResponse member = members.get(blacklist.getMemberId());
					if (member == null) {
						return null;
					}
					return BlacklistSummaryResponse.of(member, blacklist);
				})
				.filter(Objects::nonNull)
				.toList();

		return new PageImpl<>(responses, pageable, blacklists.getTotalElements());
	}

	private Page<BlacklistSummaryResponse> getActiveBlacklists(Pageable pageable) {

		Page<MemberSummaryResponse> members = findActiveMembers(pageable);
		Map<Long, MemberBlacklist> blacklists = loadBlacklists(members.getContent());

		List<BlacklistSummaryResponse> responses = members.getContent().stream()
				.map(member -> BlacklistSummaryResponse.of(member, blacklists.get(member.memberId())))
				.toList();

		return new PageImpl<>(responses, pageable, members.getTotalElements());
	}

	private Page<MemberSummaryResponse> findActiveMembers(Pageable pageable) {

		Set<Long> suspendedIds = blacklistRepository.findByStatus(BlacklistStatus.SUSPENDED).stream()
				.map(MemberBlacklist::getMemberId)
				.collect(Collectors.toSet());

		long totalMembers = fetchTotalMembers();
		long totalActive = Math.max(0, totalMembers - suspendedIds.size());

		if (totalActive == 0) {
			return new PageImpl<>(List.of(), pageable, totalActive);
		}

		List<MemberSummaryResponse> collected = new ArrayList<>();
		long targetOffset = (long) pageable.getPageNumber() * pageable.getPageSize();
		long skipped = 0;
		int pageIndex = 0;

		while (collected.size() < pageable.getPageSize()) {
			PageRequest pageRequest = PageRequest.of(pageIndex, pageable.getPageSize(), pageable.getSort());
			MemberPageResponse pageResponse = memberAdminClient.getMembers(MemberPageRequest.from(pageRequest));
			List<MemberSummaryResponse> pageContent = pageResponse.content();
			if (pageContent.isEmpty()) {
				break;
			}

			for (MemberSummaryResponse member : pageContent) {
				if (suspendedIds.contains(member.memberId())) {
					continue;
				}
				if (skipped < targetOffset) {
					skipped++;
					continue;
				}
				collected.add(member);
				if (collected.size() >= pageable.getPageSize()) {
					break;
				}
			}

			if (pageResponse.last()) {
				break;
			}
			pageIndex++;
		}

		return new PageImpl<>(collected, pageable, totalActive);
	}

	private long fetchTotalMembers() {

		MemberPageResponse members = memberAdminClient.getMembers(new MemberPageRequest(0, 1, List.of()));
		return members.totalElements();
	}

	private Map<Long, MemberBlacklist> loadBlacklists(List<MemberSummaryResponse> members) {

		List<Long> memberIds = members.stream()
				.map(MemberSummaryResponse::memberId)
				.toList();

		if (memberIds.isEmpty()) {
			return Map.of();
		}

		List<MemberBlacklist> blacklists = blacklistRepository.findByMemberIdIn(memberIds);
		return blacklists.stream()
				.collect(Collectors.toMap(MemberBlacklist::getMemberId, blacklist -> blacklist));
	}

	private Map<Long, MemberSummaryResponse> loadMembersByIds(List<Long> memberIds) {

		if (memberIds.isEmpty()) {
			return Map.of();
		}

		List<MemberSummaryResponse> members = memberAdminClient.getMembersByIds(memberIds);
		return members.stream()
				.collect(Collectors.toMap(MemberSummaryResponse::memberId, member -> member));
	}

	private MemberSummaryResponse getMemberById(Long memberId) {

		try {
			return memberAdminClient.getMember(memberId);
		} catch (FeignException.NotFound ex) {
			throw new AdminMemberNotFoundException(memberId);
		}
	}

	private MemberSummaryResponse getMemberByEmail(String email) {

		try {
			return memberAdminClient.searchByEmail(email);
		} catch (FeignException.NotFound ex) {
			throw new AdminMemberNotFoundException(email);
		}
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
