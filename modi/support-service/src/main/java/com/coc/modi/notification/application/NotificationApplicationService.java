package com.coc.modi.notification.application;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.coc.modi.common.NotificationType;
import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.notification.domain.Notification;
import com.coc.modi.notification.domain.NotificationRepository;
import com.coc.modi.notification.domain.NotificationEventDedup;
import com.coc.modi.notification.domain.NotificationEventDedupRepository;
import com.coc.modi.notification.infrastructure.client.member.MemberClientAdapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationApplicationService {

	private static final String CONSUMER_NAME = "support-service-notification";
	
	private final NotificationRepository notificationRepository;
	private final NotificationSseService notificationSseService;
	private final MemberClientAdapter memberClientAdapter;
	private final ProductModerationMailService productModerationMailService;
	private final NotificationEventDedupRepository notificationEventDedupRepository;
	
	@Transactional
	public void handle(NotificationEvent event) {
		if (!tryMarkProcessed(event)) {
			return;
		}

		Notification notification = Notification.fromEvent(event);
		Notification saved = notificationRepository.save(notification);
		notificationSseService.sendNotification(event.receiverId(), saved);
		sendModerationMail(event);
	}

	private void sendModerationMail(NotificationEvent event) {

		if (event == null || event.type() == null) {
			return;
		}

		NotificationType type;
		try {
			type = NotificationType.valueOf(event.type());
		} catch (IllegalArgumentException ex) {
			log.debug("알림 타입 파싱 실패 type={}", event.type(), ex);
			return;
		}

		if (type != NotificationType.PRODUCT_MODERATION_APPROVED
				&& type != NotificationType.PRODUCT_MODERATION_REVIEW
				&& type != NotificationType.PRODUCT_MODERATION_BLOCKED) {
			return;
		}

		String email = memberClientAdapter.getMemberEmail(event.receiverId());
		if (email == null) {
			return;
		}

		String detail = event.content();
		if (type == NotificationType.PRODUCT_MODERATION_APPROVED) {
			productModerationMailService.sendApprovedMail(email, detail);
		} else if (type == NotificationType.PRODUCT_MODERATION_REVIEW) {
			productModerationMailService.sendReviewMail(email, detail);
		} else {
			productModerationMailService.sendBlockedMail(email, detail);
		}
	}

	private boolean tryMarkProcessed(NotificationEvent event) {

		if (event == null) {
			return false;
		}
		if (!StringUtils.hasText(event.eventId())) {
			log.warn("알림 이벤트 ID가 비어 있어 중복 처리 방지를 건너뜁니다. receiverId={}", event.receiverId());
			return true;
		}

		try {
			notificationEventDedupRepository.save(NotificationEventDedup.create(event.eventId(), CONSUMER_NAME));
			return true;
		} catch (DataIntegrityViolationException ex) {
			log.info("중복 알림 이벤트 처리 건너뜀 eventId={} consumer={}", event.eventId(), CONSUMER_NAME);
			return false;
		}
	}
}
