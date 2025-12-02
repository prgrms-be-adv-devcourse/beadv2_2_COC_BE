package com.coc.modi.member.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "member", schema = "public")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @Column(nullable = false)
    private boolean emailVerified;

    private LocalDateTime lastLoginAt;

    @Column(length = 20)
    private String provider; // GOOGLE, KAKAO, NAVER

    @Column(length = 100)
    private String providerId;
}