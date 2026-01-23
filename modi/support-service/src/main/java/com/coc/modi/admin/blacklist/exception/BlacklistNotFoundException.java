package com.coc.modi.admin.blacklist.exception;

import com.coc.modi.common.ErrorCode;

public class BlacklistNotFoundException extends BlacklistException {

	public BlacklistNotFoundException(Long memberId) {
		super(ErrorCode.ADMIN_BLACKLIST_NOT_FOUND, "블랙리스트 정보를 찾을 수 없습니다. memberId=" + memberId);
	}
}
