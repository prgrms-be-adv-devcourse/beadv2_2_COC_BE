package com.coc.modi.member.member.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
	
	boolean existsByEmail(String email);
	
	boolean existsByPhone(String phone);
	
	Optional<Member> findByEmail(String email);

	Optional<Member> findByProviderAndProviderId(String provider, String providerId);
	
	Member save(Member member);
	
	Optional<Member> findById(Long memberId);

	Page<Member> findAll(Pageable pageable);

	List<Member> findByIdIn(List<Long> memberIds);
}
