package com.coc.modi.seller.chat.infrastructure;

import com.coc.modi.seller.chat.domain.ChatParticipant;
import com.coc.modi.seller.chat.domain.ChatParticipantRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatParticipantRepositoryAdapter implements ChatParticipantRepository {

	private final ChatParticipantJpaRepository chatParticipantJpaRepository;

	@Override
	public Optional<ChatParticipant> findByRoomIdAndMemberId(Long roomId, Long memberId) {
		return chatParticipantJpaRepository.findByRoomIdAndMemberId(roomId, memberId);
	}

	@Override
	public Optional<ChatParticipant> findActiveByRoomIdAndMemberId(Long roomId, Long memberId) {
		return chatParticipantJpaRepository.findByRoomIdAndMemberIdAndLeftAtIsNull(roomId, memberId);
	}

	@Override
	public List<ChatParticipant> findByRoomId(Long roomId) {

		return chatParticipantJpaRepository.findByRoomId(roomId);
	}

	@Override
	public List<ChatParticipant> findActiveByMemberId(Long memberId) {

		return chatParticipantJpaRepository.findByMemberIdAndLeftAtIsNull(memberId);
	}

	@Override
	public ChatParticipant save(ChatParticipant participant) {

		return chatParticipantJpaRepository.save(participant);
	}
}
