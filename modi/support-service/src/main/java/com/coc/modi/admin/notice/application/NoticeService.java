package com.coc.modi.admin.notice.application;

import com.coc.modi.admin.notice.application.dto.NoticeCreateCommand;
import com.coc.modi.admin.notice.application.dto.NoticeResponse;
import com.coc.modi.admin.notice.application.dto.NoticeSummaryResponse;
import com.coc.modi.admin.notice.application.dto.NoticeUpdateCommand;
import com.coc.modi.admin.notice.domain.Notice;
import com.coc.modi.admin.notice.domain.NoticeRepository;
import com.coc.modi.admin.notice.domain.NoticeStatus;
import com.coc.modi.admin.notice.exception.NoticeNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

	private final NoticeRepository noticeRepository;

	@Transactional
	public NoticeResponse createNotice(NoticeCreateCommand command) {

		validateCommand(command);

		NoticeStatus status = command.status() != null ? command.status() : NoticeStatus.DRAFT;
		if (status == NoticeStatus.DELETED) {
			throw new IllegalArgumentException("삭제 상태로 생성할 수 없습니다.");
		}

		Notice notice = Notice.create(
				command.title(),
				command.content(),
				status,
				command.pinned(),
				command.displayStartAt(),
				command.displayEndAt(),
				command.createdBy()
		);

		Notice saved = noticeRepository.save(notice);
		return NoticeResponse.from(saved);
	}

	@Transactional
	public NoticeResponse updateNotice(NoticeUpdateCommand command) {

		if (command == null || command.noticeId() == null) {
			throw new IllegalArgumentException("noticeId는 필수입니다.");
		}
		validatePeriod(command.displayStartAt(), command.displayEndAt());

		Notice notice = findNotice(command.noticeId());
		if (notice.getStatus() == NoticeStatus.DELETED) {
			throw new IllegalStateException("삭제된 공지사항은 수정할 수 없습니다.");
		}

		notice.update(
				command.title(),
				command.content(),
				command.pinned(),
				command.displayStartAt(),
				command.displayEndAt()
		);

		return NoticeResponse.from(notice);
	}

	@Transactional
	public void deleteNotice(Long noticeId) {

		Notice notice = findNotice(noticeId);
		notice.delete();
	}

	@Transactional
	public NoticeResponse publishNotice(Long noticeId) {

		Notice notice = findNotice(noticeId);
		if (notice.getStatus() == NoticeStatus.DELETED) {
			throw new IllegalStateException("삭제된 공지사항은 발행할 수 없습니다.");
		}
		notice.publish();
		return NoticeResponse.from(notice);
	}

	@Transactional
	public NoticeResponse draftNotice(Long noticeId) {

		Notice notice = findNotice(noticeId);
		if (notice.getStatus() == NoticeStatus.DELETED) {
			throw new IllegalStateException("삭제된 공지사항은 비노출로 전환할 수 없습니다.");
		}
		notice.draft();
		return NoticeResponse.from(notice);
	}

	public Page<NoticeSummaryResponse> getPublishedNotices(String keyword, Pageable pageable) {

		String normalized = normalizeKeyword(keyword);
		Page<Notice> notices = noticeRepository.findPublishedVisible(
				NoticeStatus.PUBLISHED,
				LocalDateTime.now(),
				normalized,
				pageable
		);
		return notices.map(NoticeSummaryResponse::from);
	}

	@Transactional
	public NoticeResponse getPublishedNotice(Long noticeId) {

		Notice notice = noticeRepository.findPublishedVisibleById(
						noticeId,
						NoticeStatus.PUBLISHED,
						LocalDateTime.now()
				)
				.orElseThrow(() -> new NoticeNotFoundException("공지사항을 찾을 수 없습니다. noticeId=" + noticeId));

		notice.increaseViewCount();
		return NoticeResponse.from(notice);
	}

	private Notice findNotice(Long noticeId) {

		return noticeRepository.findById(noticeId)
				.orElseThrow(() -> new NoticeNotFoundException("공지사항을 찾을 수 없습니다. noticeId=" + noticeId));
	}

	private void validateCommand(NoticeCreateCommand command) {

		if (command == null) {
			throw new IllegalArgumentException("요청 본문이 비어 있습니다.");
		}
		if (command.title() == null || command.title().isBlank()) {
			throw new IllegalArgumentException("제목은 필수입니다.");
		}
		if (command.content() == null || command.content().isBlank()) {
			throw new IllegalArgumentException("내용은 필수입니다.");
		}
		if (command.createdBy() == null) {
			throw new IllegalArgumentException("createdBy는 필수입니다.");
		}
		validatePeriod(command.displayStartAt(), command.displayEndAt());
	}

	private void validatePeriod(LocalDateTime startAt, LocalDateTime endAt) {

		if (startAt != null && endAt != null && startAt.isAfter(endAt)) {
			throw new IllegalArgumentException("노출 시작일시는 종료일시보다 이후일 수 없습니다.");
		}
	}

	private String normalizeKeyword(String keyword) {

		if (keyword == null) {
			return null;
		}
		String trimmed = keyword.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		return "%" + trimmed.toLowerCase() + "%";
	}
}
