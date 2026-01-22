package com.coc.modi.member.admin.notice.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.member.admin.notice.application.NoticeService;
import com.coc.modi.member.admin.notice.application.dto.NoticeResponse;
import com.coc.modi.member.admin.notice.application.dto.NoticeSummaryResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {

	private final NoticeService noticeService;

	@GetMapping
	public ResponseEntity<ApiResponse<Page<NoticeSummaryResponse>>> getNotices(
			@RequestParam(required = false) String keyword,
			@PageableDefault(sort = {"pinned", "createdAt"}, direction = Sort.Direction.DESC) Pageable pageable
	) {

		Page<NoticeSummaryResponse> responses = noticeService.getPublishedNotices(keyword, pageable);
		return ResponseEntity.ok(ApiResponse.ok(responses));
	}

	@GetMapping("/{noticeId}")
	public ResponseEntity<ApiResponse<NoticeResponse>> getNotice(
			@PathVariable Long noticeId
	) {

		NoticeResponse response = noticeService.getPublishedNotice(noticeId);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
}
