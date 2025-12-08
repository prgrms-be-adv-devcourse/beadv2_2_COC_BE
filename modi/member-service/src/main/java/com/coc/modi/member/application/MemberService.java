package com.coc.modi.member.application;

import com.coc.modi.member.application.dto.CreateMemberCommand;
import com.coc.modi.member.application.dto.MemberProfileResponse;
import com.coc.modi.member.application.dto.MemberSignupResponse;
import com.coc.modi.member.domain.Member;
import com.coc.modi.member.domain.MemberRole;
import com.coc.modi.member.domain.MemberRepository;
import com.coc.modi.member.application.dto.UpdateMemberCommand;
import com.coc.modi.member.infrastructure.client.AccountFeignClient;
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
    private final AccountFeignClient accountFeignClient;

    // 회원가입
    @Transactional
    public MemberSignupResponse signup(CreateMemberCommand command){

        validateEmail(command.email());

        if(memberRepository.existsByEmail(command.email())){

            throw new IllegalArgumentException("Email already exists");
        }

        validatePassword(command.password());

        validatePhone(command.phone());

        String encodedPassword = passwordEncoder.encode(command.password());

        Member member = Member.create(
                command.email(),
                encodedPassword,
                command.name(),
                command.phone(),
                MemberRole.USER
        );

        Member saved = memberRepository.save(member);

        // 회원 지갑 생성 요청
        accountFeignClient.createWallet(saved.getId());

        return MemberSignupResponse.from(saved);
    }

    // 내 정보 조회
    @Transactional(readOnly = true)
    public MemberProfileResponse getProfile(Authentication authentication){

        Long memberId = (Long) authentication.getPrincipal();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));

        return MemberProfileResponse.from(member);
    }

    // 회원 정보 수정
    @Transactional
    public MemberProfileResponse updateProfile(Authentication authentication,
                                               UpdateMemberCommand command) {

        Long memberId = (Long) authentication.getPrincipal();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));

        if (command.name() != null && !command.name().isBlank()) {

            member.changeName(command.name());
        }

        if (command.phone() != null && !command.phone().isBlank()) {

            validatePhone(command.phone());
            member.changePhone(command.phone());
        }

        if (command.newPassword() != null && !command.newPassword().isBlank()) {

            validatePassword(command.newPassword());
            String encodedPassword = passwordEncoder.encode(command.newPassword());
            member.changePassword(encodedPassword);
        }

        return MemberProfileResponse.from(member);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteMember(Authentication authentication) {

        Long memberId = (Long) authentication.getPrincipal();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));

        member.withdraw();
    }

    // 비밀번호 유효성 검사
    private void validatePassword(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        if (password.length() < 8 || password.length() > 20) {
            throw new IllegalArgumentException("비밀번호는 8자 이상 20자 이하로 입력해주세요.");
        }

        if (!password.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException("비밀번호에는 영문자가 1개 이상 포함되어야 합니다.");
        }

        if(!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("비밀번호에는 숫자가 1개 이상 포함되어야 합니다.");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-={}:\";'<>?,./].*")) {
            throw new IllegalArgumentException("비밀번호에는 특수문자가 최소 1개 이상 포함되어야 합니다.");
        }
    }

    // 이메일 유효성 검사
    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }
    }

    // 전화번호 유효성 검사
    private void validatePhone(String phone) {
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
