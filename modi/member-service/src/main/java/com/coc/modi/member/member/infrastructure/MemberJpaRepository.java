package com.coc.modi.member.member.infrastructure;

import com.coc.modi.member.member.domain.Member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
	
	boolean existsByEmail(String email);
	
	Optional<Member> findByEmail(String email);
}
