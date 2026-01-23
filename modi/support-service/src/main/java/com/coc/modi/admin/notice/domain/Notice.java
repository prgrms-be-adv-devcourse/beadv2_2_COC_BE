package com.coc.modi.admin.notice.domain;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "notice", schema = "support")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NoticeStatus status;

	@Column(nullable = false)
	private boolean pinned;

	@Column(nullable = false)
	private long viewCount;

	@Column(name = "display_start_at")
	private LocalDateTime displayStartAt;

	@Column(name = "display_end_at")
	private LocalDateTime displayEndAt;

	@Column(name = "created_by", nullable = false)
	private Long createdBy;

	private Notice(String title,
				   String content,
				   NoticeStatus status,
				   boolean pinned,
				   LocalDateTime displayStartAt,
				   LocalDateTime displayEndAt,
				   Long createdBy) {

		this.title = title;
		this.content = content;
		this.status = status;
		this.pinned = pinned;
		this.viewCount = 0L;
		this.displayStartAt = displayStartAt;
		this.displayEndAt = displayEndAt;
		this.createdBy = createdBy;
	}

	public static Notice create(String title,
								String content,
								NoticeStatus status,
								boolean pinned,
								LocalDateTime displayStartAt,
								LocalDateTime displayEndAt,
								Long createdBy) {

		return new Notice(title, content, status, pinned, displayStartAt, displayEndAt, createdBy);
	}

	public void update(String title,
					   String content,
					   Boolean pinned,
					   LocalDateTime displayStartAt,
					   LocalDateTime displayEndAt) {

		if (title != null) {
			this.title = title;
		}
		if (content != null) {
			this.content = content;
		}
		if (pinned != null) {
			this.pinned = pinned;
		}
		if (displayStartAt != null) {
			this.displayStartAt = displayStartAt;
		}
		if (displayEndAt != null) {
			this.displayEndAt = displayEndAt;
		}
	}

	public void publish() {

		this.status = NoticeStatus.PUBLISHED;
	}

	public void draft() {

		this.status = NoticeStatus.DRAFT;
	}

	public void delete() {

		this.status = NoticeStatus.DELETED;
	}

	public void increaseViewCount() {

		this.viewCount += 1;
	}
}
