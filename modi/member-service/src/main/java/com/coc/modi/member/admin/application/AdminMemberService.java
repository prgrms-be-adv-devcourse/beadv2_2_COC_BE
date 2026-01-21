package com.coc.modi.member.admin.application;

import com.coc.modi.member.admin.application.dto.AdminMemberCreateCommand;
import com.coc.modi.member.admin.application.dto.AdminMemberCreateResponse;
import com.coc.modi.member.member.domain.Member;
import com.coc.modi.member.member.domain.MemberRepository;
import com.coc.modi.member.member.domain.MemberRole;
import com.coc.modi.member.member.exception.EmailDuplicatedException;
import com.coc.modi.member.member.exception.PhoneDuplicatedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminMemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public AdminMemberCreateResponse createAdmin(AdminMemberCreateCommand command, Long createdByMemberId) {

		if (memberRepository.existsByEmail(command.email())) {

			throw new EmailDuplicatedException(command.email());
		}

		if (memberRepository.existsByPhone(command.phone())) {

			throw new PhoneDuplicatedException(command.phone());
		}

		String encodedPassword = passwordEncoder.encode(command.password());

		Member member = Member.create(
				command.email(),
				encodedPassword,
				command.name(),
				command.phone(),
				MemberRole.ADMIN
		);

		Member saved = memberRepository.save(member);

		log.info("admin_account_created createdBy={} targetMemberId={}", createdByMemberId, saved.getId());

		return AdminMemberCreateResponse.from(saved);
	}
}
