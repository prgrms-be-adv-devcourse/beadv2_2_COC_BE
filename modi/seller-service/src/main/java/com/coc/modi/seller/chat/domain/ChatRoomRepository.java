package com.coc.modi.seller.chat.domain;

import java.util.Optional;

public interface ChatRoomRepository {

	Optional<ChatRoom> findById(Long roomId);

	Optional<ChatRoom> findByRoomKey(String roomKey);

	ChatRoom save(ChatRoom room);
}
