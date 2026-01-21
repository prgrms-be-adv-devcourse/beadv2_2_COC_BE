package com.coc.modi.member.member.infrastructure;

import com.coc.modi.member.member.domain.Member;
import com.coc.modi.member.member.domain.MemberRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryAdapter implements MemberRepository {
	
	private final MemberJpaRepository memberJpaRepository;
	
	@Override
	public boolean existsByEmail(String email) {
		
		return memberJpaRepository.existsByEmail(email);
	}
	
	@Override
	public boolean existsByPhone(String phone) {
		
		return memberJpaRepository.existsByPhone(phone);
	}
	
	@Override
	public Optional<Member> findByEmail(String email) {
		
		return memberJpaRepository.findByEmail(email);
	}

	@Override
	public Optional<Member> findByProviderAndProviderId(String provider, String providerId) {

		return memberJpaRepository.findByProviderAndProviderId(provider, providerId);
	}
	
	@Override
	public Member save(Member member) {
		
		return memberJpaRepository.save(member);
	}
	
	@Override
	public Optional<Member> findById(Long memberId) {
		
		return memberJpaRepository.findById(memberId);
	}

	@Override
	public Page<Member> findAll(Pageable pageable) {

		return memberJpaRepository.findAll(pageable);
	}

	@Override
	public List<Member> findByIdIn(List<Long> memberIds) {

		return memberJpaRepository.findByIdIn(memberIds);
	}
}
