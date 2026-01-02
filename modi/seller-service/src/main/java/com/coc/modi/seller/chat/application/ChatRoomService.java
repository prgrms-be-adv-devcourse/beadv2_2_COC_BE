package com.coc.modi.seller.chat.application;

import com.coc.modi.seller.chat.application.dto.ChatRoomCreateCommand;
import com.coc.modi.seller.chat.application.dto.ChatRoomResponse;
import com.coc.modi.seller.chat.domain.ChatParticipant;
import com.coc.modi.seller.chat.domain.ChatParticipantRepository;
import com.coc.modi.seller.chat.domain.ChatParticipantRole;
import com.coc.modi.seller.chat.domain.ChatRoom;
import com.coc.modi.seller.chat.domain.ChatRoomRepository;
import com.coc.modi.seller.chat.exception.ChatAccessDeniedException;
import com.coc.modi.seller.chat.exception.ChatInputInvalidException;
import com.coc.modi.seller.chat.exception.ChatRoomNotFoundException;
import com.coc.modi.seller.chat.exception.ChatRoomStateConflictException;
import com.coc.modi.seller.seller.exception.SellerNotFoundException;
import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final SellerRepository sellerRepository;

    @Transactional
	public ChatRoomResponse createRoom(ChatRoomCreateCommand command) {
        if (command == null || command.requesterMemberId() == null || command.requesterRole() == null) {
            throw new ChatInputInvalidException("요청자 정보가 필요합니다.");
        }

		ChatRoomParticipants participants = resolveParticipants(command);
		String roomKey = buildRoomKey(participants.seller().getId(), participants.memberId());

        ChatRoom room = chatRoomRepository.findByRoomKey(roomKey)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.create(roomKey)));

        ensureParticipant(room, participants.seller().getMemberId(), ChatParticipantRole.SELLER);
        ensureParticipant(room, participants.memberId(), ChatParticipantRole.MEMBER);

		return ChatRoomResponse.from(
				room,
				participants.seller().getId(),
				participants.memberId());
	}

    public ChatRoomResponse getRoom(Long roomId, Long requesterMemberId) {
        if (roomId == null || requesterMemberId == null) {
            throw new ChatInputInvalidException("roomId와 memberId는 필수입니다.");
        }

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다. roomId=" + roomId));

        chatParticipantRepository.findByRoomIdAndMemberId(roomId, requesterMemberId)
                .orElseThrow(() -> new ChatAccessDeniedException("채팅방 접근 권한이 없습니다. roomId=" + roomId));

        return toResponse(room);
    }

	private ChatRoomParticipants resolveParticipants(ChatRoomCreateCommand command) {
		String role = command.requesterRole().trim().toUpperCase();
		Long requesterMemberId = command.requesterMemberId();

		if ("SELLER".equals(role)) {
			if (command.memberId() == null) {
				throw new ChatInputInvalidException("판매자 요청에는 memberId가 필요합니다.");
			}
			Seller seller = findSellerByMemberId(requesterMemberId);
			validateDistinctParticipants(seller.getMemberId(), command.memberId());

			return new ChatRoomParticipants(seller, command.memberId());
		}

		if ("MEMBER".equals(role)) {
			if (command.sellerId() == null) {
				throw new ChatInputInvalidException("회원 요청에는 sellerId가 필요합니다.");
			}
			Seller seller = sellerRepository.findById(command.sellerId())
					.orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다. id=" + command.sellerId()));
			validateDistinctParticipants(seller.getMemberId(), requesterMemberId);

			return new ChatRoomParticipants(seller, requesterMemberId);
		}

        throw new ChatInputInvalidException("지원하지 않는 역할입니다. role=" + command.requesterRole());
    }

	private Seller findSellerByMemberId(Long memberId) {

		return sellerRepository.findByMemberId(memberId)
				.orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다. memberId=" + memberId));
	}

    private void validateDistinctParticipants(Long sellerOwnerId, Long memberId) {
        if (sellerOwnerId.equals(memberId)) {
            throw new ChatInputInvalidException("판매자와 회원이 동일할 수 없습니다.");
        }
    }

    private String buildRoomKey(Long sellerId, Long memberId) {

        return sellerId + ":" + memberId;
    }

    private void ensureParticipant(ChatRoom room, Long memberId, ChatParticipantRole role) {
        chatParticipantRepository.findByRoomIdAndMemberId(room.getId(), memberId)
                .orElseGet(() -> chatParticipantRepository.save(
                        ChatParticipant.builder()
                                .room(room)
                                .memberId(memberId)
                                .role(role)
                                .build()
                ));
    }

	private ChatRoomResponse toResponse(ChatRoom room) {
		List<ChatParticipant> participants = chatParticipantRepository.findByRoomId(room.getId());

		Long sellerAccountId = null;
		Long memberId = null;

        for (ChatParticipant participant : participants) {
            if (participant.getRole() == ChatParticipantRole.SELLER) {
                sellerAccountId = participant.getMemberId();
            } else if (participant.getRole() == ChatParticipantRole.MEMBER) {
                memberId = participant.getMemberId();
            }
        }

			if (sellerAccountId == null || memberId == null) {
				throw new ChatRoomStateConflictException("채팅방 참가자 정보가 올바르지 않습니다. roomId=" + room.getId());
			}

		Seller seller = findSellerByMemberId(sellerAccountId);

		return ChatRoomResponse.from(room, seller.getId(), memberId);
	}

	private record ChatRoomParticipants(Seller seller, Long memberId) {
	}
}
