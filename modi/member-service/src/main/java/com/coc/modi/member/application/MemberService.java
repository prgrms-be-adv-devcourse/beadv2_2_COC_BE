package com.coc.modi.member.application;

import com.coc.modi.member.application.dto.CreateMemberCommand;
import com.coc.modi.member.application.dto.MemberProfileResponse;
import com.coc.modi.member.application.dto.MemberSignupResponse;
import com.coc.modi.member.domain.Member;
import com.coc.modi.member.domain.MemberRole;
import com.coc.modi.member.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberSignupResponse signup(CreateMemberCommand command){

        if(memberRepository.existsByEmail(command.email())){

            throw new IllegalArgumentException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(command.password());

        Member member = Member.create(
                command.email(),
                encodedPassword,
                command.name(),
                command.phone(),
                MemberRole.USER
        );

        Member saved = memberRepository.save(member);

        return new MemberSignupResponse(
                saved.getEmail(),
                saved.getName(),
                saved.getPhone(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public MemberProfileResponse getProfile(Authentication authentication){

        Long memberId = (Long) authentication.getPrincipal();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));

        return new MemberProfileResponse(
                member.getEmail(),
                member.getName(),
                member.getPhone(),
                member.getCreatedAt()
        );
    }
}
