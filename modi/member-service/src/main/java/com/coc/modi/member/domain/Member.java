package com.coc.modi.account.member.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member", schema = "public")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 20)
    private String provider;

    @Column(length = 100)
    private String providerId;
<<<<<<<< HEAD:modi/account-service/src/main/java/com/coc/modi/account/member/domain/Member.java
========

    private Member(String email,
                   String password,
                   String name,
                   String phone,
                   MemberRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.status = MemberStatus.ACTIVE;
    }

    public static Member create(String email,
                                String password,
                                String name,
                                String phone,
                                MemberRole role) {
        return new Member(email, password, name, phone, role);
    }
>>>>>>>> dev:modi/member-service/src/main/java/com/coc/modi/member/domain/Member.java
}
