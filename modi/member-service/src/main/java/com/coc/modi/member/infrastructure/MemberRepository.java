package com.coc.modi.member.infrastructure;

import com.coc.modi.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    public boolean existsByEmail(String email) {

        return memberJpaRepository.existsByEmail(email);
    }

    public Optional<Member> findByEmail(String email) {

        return memberJpaRepository.findByEmail(email);
    }

    public Member save(Member member) {

        return memberJpaRepository.save(member);
    }

    public Optional<Member> findById(Long memberId) {

        return memberJpaRepository.findById(memberId);
    }
}
