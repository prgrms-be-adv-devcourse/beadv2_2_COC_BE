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
public class SellerApprovalMailService {
	
	private static final String APPROVED_SUBJECT = "판매자 등록 승인";
	private static final String APPROVED_BODY = """
			안녕하세요, MODI입니다.
			
			판매자 등록이 승인되었습니다.
			로그인 후 판매자 메뉴에서 확인해 주세요.
			""";
	
	private static final String REJECTED_SUBJECT = "판매자 등록 거절";
	private static final String REJECTED_BODY = """
			안녕하세요, MODI입니다.
			
			판매자 등록이 거절되었습니다.
			자세한 내용은 고객센터에 문의해주세요.
			""";
	
	private final JavaMailSender mailSender;
	
	@Value("${mail.from-address}")
	private String fromAddress;
	
	public void sendApprovedMail(String email) {
		
		sendMail(email, APPROVED_SUBJECT, APPROVED_BODY);
	}
	
	public void sendRejectedMail(String email) {
		
		sendMail(email, REJECTED_SUBJECT, REJECTED_BODY);
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
