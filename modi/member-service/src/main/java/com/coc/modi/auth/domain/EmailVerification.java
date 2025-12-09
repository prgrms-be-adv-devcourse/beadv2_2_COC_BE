package com.coc.modi.auth.domain;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "email_verification", schema = "public")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, length = 320, unique = true)
	private String email;
	
	@Column(nullable = false, length = 10)
	private String code;
	
	@Column(nullable = false)
	private LocalDateTime expiresAt;
	
	@Column(nullable = false)
	private boolean verified;
	
	private EmailVerification(String email,
							  String code,
							  LocalDateTime expiresAt) {
		
		this.email = email;
		this.code = code;
		this.expiresAt = expiresAt;
		this.verified = false;
	}
	
	public static EmailVerification create(String email,
										   String code,
										   LocalDateTime expiresAt) {
		
		return new EmailVerification(email, code, expiresAt);
	}
	
	public void regenerate(String code,
						   LocalDateTime expiresAt) {
		
		this.code = code;
		this.expiresAt = expiresAt;
		this.verified = false;
	}
	
	public void verify(String code,
					   LocalDateTime currentTime) {
		
		if (isExpired(currentTime)) {
			
			throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
		}
		
		if (!this.code.equals(code)) {
			
			throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
		}
		
		this.verified = true;
	}
	
	private boolean isExpired(LocalDateTime currentTime) {
		
		return currentTime.isAfter(expiresAt);
	}
}
