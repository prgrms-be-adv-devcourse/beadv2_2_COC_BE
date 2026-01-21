package com.coc.modi.seller.seller.application;

import java.util.Optional;

import com.coc.modi.kafka.event.SellerApprovedEvent;
import com.coc.modi.kafka.event.SellerRejectedEvent;
import com.coc.modi.seller.outbox.SellerOutboxService;
import com.coc.modi.seller.seller.application.dto.SellerDetailResponse;
import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import com.coc.modi.seller.seller.domain.SellerStatus;
import com.coc.modi.seller.seller.infrastructure.client.member.MemberClientAdapter;
import com.coc.modi.seller.seller.infrastructure.client.member.dto.MemberEmailResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerApprovalServiceTest {

	@Mock
	private SellerRepository sellerRepository;

	@Mock
	private SellerOutboxService sellerOutboxService;

	@Mock
	private MemberClientAdapter memberClientAdapter;

	@InjectMocks
	private SellerApprovalService sellerApprovalService;

	@Test
	void approveSeller_enqueuesOutboxWhenPending() {

		Seller seller = Seller.create(10L, "store-10", "biz-10", "010-0000-0000");
		ReflectionTestUtils.setField(seller, "id", 1L);

		when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
		when(memberClientAdapter.getMemberEmail(10L)).thenReturn(new MemberEmailResponse(10L, "seller10@example.com"));

		SellerDetailResponse response = sellerApprovalService.approveSeller(1L);

		verify(sellerRepository).save(seller);
		ArgumentCaptor<SellerApprovedEvent> captor = ArgumentCaptor.forClass(SellerApprovedEvent.class);
		verify(sellerOutboxService).enqueueSellerApproved(captor.capture());
		assertThat(captor.getValue().sellerId()).isEqualTo(1L);
		assertThat(captor.getValue().memberId()).isEqualTo(10L);
		assertThat(captor.getValue().email()).isEqualTo("seller10@example.com");
		assertThat(response.status()).isEqualTo(SellerStatus.ACTIVE);
	}

	@Test
	void approveSeller_skipsOutboxWhenAlreadyActive() {

		Seller seller = Seller.create(11L, "store-11", "biz-11", "010-0000-0001");
		seller.approve();
		ReflectionTestUtils.setField(seller, "id", 2L);

		when(sellerRepository.findById(2L)).thenReturn(Optional.of(seller));

		sellerApprovalService.approveSeller(2L);

		verify(sellerRepository).save(seller);
		verify(sellerOutboxService, never()).enqueueSellerApproved(any(SellerApprovedEvent.class));
	}

	@Test
	void rejectSeller_enqueuesOutboxWhenPending() {

		Seller seller = Seller.create(12L, "store-12", "biz-12", "010-0000-0002");
		ReflectionTestUtils.setField(seller, "id", 3L);

		when(sellerRepository.findById(3L)).thenReturn(Optional.of(seller));
		when(memberClientAdapter.getMemberEmail(12L)).thenReturn(new MemberEmailResponse(12L, "seller12@example.com"));

		SellerDetailResponse response = sellerApprovalService.rejectSeller(3L);

		verify(sellerRepository).save(seller);
		ArgumentCaptor<SellerRejectedEvent> captor = ArgumentCaptor.forClass(SellerRejectedEvent.class);
		verify(sellerOutboxService).enqueueSellerRejected(captor.capture());
		assertThat(captor.getValue().sellerId()).isEqualTo(3L);
		assertThat(captor.getValue().memberId()).isEqualTo(12L);
		assertThat(captor.getValue().email()).isEqualTo("seller12@example.com");
		assertThat(response.status()).isEqualTo(SellerStatus.REJECTED);
	}

	@Test
	void rejectSeller_skipsOutboxWhenAlreadyRejected() {

		Seller seller = Seller.create(13L, "store-13", "biz-13", "010-0000-0003");
		seller.reject();
		ReflectionTestUtils.setField(seller, "id", 4L);

		when(sellerRepository.findById(4L)).thenReturn(Optional.of(seller));

		sellerApprovalService.rejectSeller(4L);

		verify(sellerRepository).save(seller);
		verify(sellerOutboxService, never()).enqueueSellerRejected(any(SellerRejectedEvent.class));
	}
}
