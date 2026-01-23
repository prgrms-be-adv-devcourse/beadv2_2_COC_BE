package com.coc.modi.seller.chat.infrastructure;

import com.coc.modi.seller.chat.domain.ChatParticipant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantJpaRepository extends JpaRepository<ChatParticipant, Long> {

	Optional<ChatParticipant> findByRoomIdAndMemberId(Long roomId, Long memberId);

	Optional<ChatParticipant> findByRoomIdAndMemberIdAndLeftAtIsNull(Long roomId, Long memberId);

	List<ChatParticipant> findByRoomId(Long roomId);

	List<ChatParticipant> findByMemberIdAndLeftAtIsNull(Long memberId);
}
