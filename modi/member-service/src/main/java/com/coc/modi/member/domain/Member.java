package com.coc.modi.member.domain;

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

    public void changeName(String name) {

        this.name = name;
    }

    public void changePhone(String phone) {

        this.phone = phone;
    }

    public void changePassword(String password) {

        this.password = password;
    }

    public void withdraw() {

        this.status = MemberStatus.WITHDRAWN;
    }
}
