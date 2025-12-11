package com.coc.modi.review.exception;

import com.coc.modi.common.ErrorCode;

public class ReviewAccessDeniedException extends ReviewException {

	public ReviewAccessDeniedException() {
		
		super(ErrorCode.REVIEW_FORBIDDEN, "리뷰 작성자만 수정/삭제할 수 있습니다.");
	}
}
