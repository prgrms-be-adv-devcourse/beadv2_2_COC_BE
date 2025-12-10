package com.coc.modi.member.member.application;

import org.springframework.stereotype.Component;

@Component
public class MemberValidationService {
	
	// 비밀번호 유효성 검사
	public void validatePassword(String password) {
		
		if (password == null || password.isBlank()) {
			
			throw new IllegalArgumentException("비밀번호를 입력해주세요.");
		}
		
		if (password.length() < 8 || password.length() > 20) {
			
			throw new IllegalArgumentException("비밀번호는 8자 이상 20자 이하로 입력해주세요.");
		}
		
		if (!password.matches(".*[A-Za-z].*")) {
			
			throw new IllegalArgumentException("비밀번호에는 영문자가 1개 이상 포함되어야 합니다.");
		}
		
		if (!password.matches(".*\\d.*")) {
			
			throw new IllegalArgumentException("비밀번호에는 숫자가 1개 이상 포함되어야 합니다.");
		}
		
		if (!password.matches(".*[!@#$%^&*()_+\\-={}:\";'<>?,./].*")) {
			
			throw new IllegalArgumentException("비밀번호에는 특수문자가 최소 1개 이상 포함되어야 합니다.");
		}
	}
	
	// 이메일 유효성 검사
	public void validateEmail(String email) {
		
		if (email == null || email.isBlank()) {
			
			throw new IllegalArgumentException("이메일을 입력해주세요.");
		}
		
		String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		
		if (!email.matches(emailRegex)) {
			
			throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
		}
	}
	
	// 휴대폰 번호 유효성 검사
	public void validatePhone(String phone) {
		
		if (phone == null || phone.isBlank()) {
			
			throw new IllegalArgumentException("전화번호를 입력해주세요.");
		}
		
		String digits = phone.replaceAll("[^0-9]", "");
		
		if (digits.length() < 9 || digits.length() > 11) {
			
			throw new IllegalArgumentException("전화번호는 숫자 9~11자리여야 합니다.");
		}
		
		if (!digits.startsWith("0")) {
			
			throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다.");
		}
	}
}
