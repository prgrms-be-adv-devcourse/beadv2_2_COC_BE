package com.coc.modi.member.member.domain;

import java.util.Optional;

public interface MemberRepository {
	
	boolean existsByEmail(String email);
	
	Optional<Member> findByEmail(String email);
	
	Member save(Member member);
	
	Optional<Member> findById(Long memberId);
}
