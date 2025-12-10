package com.coc.modi.review.exception;

import com.coc.modi.common.ErrorCode;

public class ReviewNotFoundException extends ReviewException {

	public ReviewNotFoundException(Long reviewId) {
		
		super(ErrorCode.REVIEW_NOT_FOUND, "리뷰를 찾을 수 없습니다. reviewId: " + reviewId);
	}
}
