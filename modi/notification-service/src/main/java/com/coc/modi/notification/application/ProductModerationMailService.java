package com.coc.modi.notification.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductModerationMailService {

	private static final String REVIEW_SUBJECT = "상품 검토 필요";
	private static final String REVIEW_BODY = """
			안녕하세요, MODI입니다.

			등록하신 상품에 확인이 필요한 내용이 있습니다.
			판매자 페이지에서 상품 내용을 확인해 주세요.
			""";

	private static final String BLOCKED_SUBJECT = "상품 등록 차단";
	private static final String BLOCKED_BODY = """
			안녕하세요, MODI입니다.

			등록하신 상품이 정책 위반으로 차단되었습니다.
			자세한 내용은 고객센터에 문의해 주세요.
			""";
	private static final String APPROVED_SUBJECT = "상품 등록 승인";
	private static final String APPROVED_BODY = """
			안녕하세요, MODI입니다.

			등록하신 상품이 승인되었습니다.
			판매자 페이지에서 상품 상태를 확인해 주세요.
			""";

	private final JavaMailSender mailSender;

	@Value("${mail.from-address}")
	private String fromAddress;

	public void sendReviewMail(String email, String detail) {

		sendMail(email, REVIEW_SUBJECT, appendDetail(REVIEW_BODY, detail));
	}

	public void sendBlockedMail(String email, String detail) {

		sendMail(email, BLOCKED_SUBJECT, appendDetail(BLOCKED_BODY, detail));
	}

	public void sendApprovedMail(String email, String detail) {

		sendMail(email, APPROVED_SUBJECT, appendDetail(APPROVED_BODY, detail));
	}

	private String appendDetail(String base, String detail) {

		if (!StringUtils.hasText(detail)) {
			return base;
		}
		return base + "\n\n" + "알림 내용: " + detail;
	}

	private void sendMail(String email, String subject, String body) {

		if (!StringUtils.hasText(email)) {
			log.warn("메일 전송 대상 이메일이 비어있습니다.");
			return;
		}

		if (!StringUtils.hasText(fromAddress)) {
			log.warn("메일 발신 주소가 설정되지 않았습니다.");
			return;
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setFrom(fromAddress);
		message.setSubject(subject);
		message.setText(body);

		try {
			mailSender.send(message);
		} catch (MailException ex) {
			log.warn("메일 전송 실패 email={}", email, ex);
		}
	}
}
