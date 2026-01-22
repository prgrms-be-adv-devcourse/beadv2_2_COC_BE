package com.coc.modi.admin.application;

import com.coc.modi.admin.application.dto.AdminMemberCreateCommand;
import com.coc.modi.admin.application.dto.AdminMemberCreateResponse;
import com.coc.modi.admin.infrastructure.client.member.MemberAdminClient;
import com.coc.modi.admin.infrastructure.client.member.dto.AdminMemberCreateInternalRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminMemberService {

	private final MemberAdminClient memberAdminClient;

	@Transactional
	public AdminMemberCreateResponse createAdmin(AdminMemberCreateCommand command, Long createdByMemberId) {

		if (command == null) {
			throw new IllegalArgumentException("요청 본문이 비어 있습니다.");
		}
		if (createdByMemberId == null) {
			throw new IllegalArgumentException("createdBy는 필수입니다.");
		}

		AdminMemberCreateInternalRequest request = new AdminMemberCreateInternalRequest(
				command.email(),
				command.password(),
				command.name(),
				command.phone(),
				createdByMemberId
		);

		AdminMemberCreateResponse response = memberAdminClient.createAdmin(request);
		log.info("admin_account_created createdBy={} targetMemberId={}", createdByMemberId, response.memberId());
		return response;
	}
}
