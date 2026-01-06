package com.coc.modi.seller.chat.domain;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository {

	Optional<ChatParticipant> findByRoomIdAndMemberId(Long roomId, Long memberId);

	List<ChatParticipant> findByRoomId(Long roomId);

	ChatParticipant save(ChatParticipant participant);
}
