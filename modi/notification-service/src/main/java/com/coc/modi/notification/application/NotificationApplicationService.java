package com.coc.modi.notification.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coc.modi.common.NotificationType;
import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.notification.domain.Notification;
import com.coc.modi.notification.domain.NotificationRepository;
import com.coc.modi.notification.infrastructure.client.member.MemberClientAdapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationApplicationService {
	
	private final NotificationRepository notificationRepository;
	private final NotificationSseService notificationSseService;
	private final MemberClientAdapter memberClientAdapter;
	private final ProductModerationMailService productModerationMailService;
	
	@Transactional
	public void handle(NotificationEvent event) {
		
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
}
