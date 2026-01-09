package com.coc.modi.seller.seller.application;

import java.util.Optional;

import com.coc.modi.kafka.event.SellerApprovedEvent;
import com.coc.modi.seller.outbox.SellerOutboxService;
import com.coc.modi.seller.seller.application.dto.SellerDetailResponse;
import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import com.coc.modi.seller.seller.domain.SellerStatus;

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

	@InjectMocks
	private SellerApprovalService sellerApprovalService;

	@Test
	void approveSeller_enqueuesOutboxWhenPending() {

		Seller seller = Seller.create(10L, "store-10", "biz-10", "010-0000-0000");
		ReflectionTestUtils.setField(seller, "id", 1L);

		when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));

		SellerDetailResponse response = sellerApprovalService.approveSeller(1L);

		verify(sellerRepository).save(seller);
		ArgumentCaptor<SellerApprovedEvent> captor = ArgumentCaptor.forClass(SellerApprovedEvent.class);
		verify(sellerOutboxService).enqueueSellerApproved(captor.capture());
		assertThat(captor.getValue().sellerId()).isEqualTo(1L);
		assertThat(captor.getValue().memberId()).isEqualTo(10L);
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
}
