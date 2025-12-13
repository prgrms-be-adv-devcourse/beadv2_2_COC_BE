package com.coc.modi.member.auth.infrastructure.mail;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class EmailSender {
	
	private final JavaMailSender mailSender;
	
	@Value("${mail.from-address}")
	private String fromAddress;
	
	public void sendVerificationCode(String to, String code) {
		
		if (!StringUtils.hasText(fromAddress)) {
			
			throw new IllegalStateException("메일 발신 주소가 설정되지 않았습니다.");
		}
		
		SimpleMailMessage message = new SimpleMailMessage();
		
		message.setTo(to);
		message.setFrom(fromAddress);
		message.setSubject("MODI 이메일 인증 코드");
		message.setText(buildBody(code));
		
		mailSender.send(message);
	}
	
	private String buildBody(String code) {
		
		return """
				안녕하세요, MODI입니다.
				
				요청하신 인증 코드는 [%s] 입니다.
				5분 안에 입력해 주세요.
				""".formatted(code);
	}
}
