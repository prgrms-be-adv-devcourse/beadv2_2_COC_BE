package com.coc.modi.member.member.domain;

import java.util.Optional;

public interface MemberRepository {
	
	boolean existsByEmail(String email);
	
	boolean existsByPhone(String phone);
	
	Optional<Member> findByEmail(String email);
	
	Member save(Member member);
	
	Optional<Member> findById(Long memberId);
}
