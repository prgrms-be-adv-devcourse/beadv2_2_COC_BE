package com.coc.modi.seller.seller.application;

import java.util.Optional;

import com.coc.modi.kafka.event.SellerRegistrationApprovedEvent;
import com.coc.modi.kafka.event.SellerRegistrationRejectedEvent;
import com.coc.modi.seller.outbox.SellerOutboxService;
import com.coc.modi.seller.seller.application.dto.SellerRegistrationResponse;
import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import com.coc.modi.seller.seller.infrastructure.client.member.MemberClientAdapter;
import com.coc.modi.seller.seller.infrastructure.client.member.dto.MemberEmailResponse;
import com.coc.modi.seller.seller.registration.domain.SellerRegistration;
import com.coc.modi.seller.seller.registration.domain.SellerRegistrationRepository;
import com.coc.modi.seller.seller.registration.domain.SellerRegistrationStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerApprovalServiceTest {

	@Mock
	private SellerRepository sellerRepository;

	@Mock
	private SellerRegistrationRepository sellerRegistrationRepository;

	@Mock
	private SellerOutboxService sellerOutboxService;

	@Mock
	private MemberClientAdapter memberClientAdapter;

	@InjectMocks
	private SellerApprovalService sellerApprovalService;

	@Test
	void approveSeller_enqueuesOutboxWhenPending() {

		SellerRegistration registration = SellerRegistration.create(10L, "store-10", "biz-10", "010-0000-0000");
		ReflectionTestUtils.setField(registration, "id", 100L);

		when(sellerRegistrationRepository.findByMemberId(10L)).thenReturn(Optional.of(registration));
		when(sellerRepository.existsByMemberId(10L)).thenReturn(false);
		when(memberClientAdapter.getMemberEmail(10L)).thenReturn(new MemberEmailResponse(10L, "seller10@example.com"));
		doAnswer(invocation -> {
			Seller seller = invocation.getArgument(0);
			ReflectionTestUtils.setField(seller, "id", 1L);
			return seller;
		}).when(sellerRepository).save(any(Seller.class));

		SellerRegistrationResponse response = sellerApprovalService.approveSeller(10L, 99L);

		verify(sellerRepository).save(any(Seller.class));
		verify(sellerRegistrationRepository).save(registration);
		ArgumentCaptor<SellerRegistrationApprovedEvent> captor =
				ArgumentCaptor.forClass(SellerRegistrationApprovedEvent.class);
		verify(sellerOutboxService).enqueueSellerApproved(captor.capture());
		assertThat(captor.getValue().registrationId()).isEqualTo(100L);
		assertThat(captor.getValue().memberId()).isEqualTo(10L);
		assertThat(captor.getValue().email()).isEqualTo("seller10@example.com");
		assertThat(response.status()).isEqualTo(SellerRegistrationStatus.APPROVED);
		assertThat(response.approvedBy()).isEqualTo(99L);
	}

	@Test
	void rejectSeller_enqueuesOutboxWhenPending() {

		SellerRegistration registration = SellerRegistration.create(12L, "store-12", "biz-12", "010-0000-0002");
		ReflectionTestUtils.setField(registration, "id", 200L);

		when(sellerRegistrationRepository.findByMemberId(12L)).thenReturn(Optional.of(registration));
		when(memberClientAdapter.getMemberEmail(12L)).thenReturn(new MemberEmailResponse(12L, "seller12@example.com"));

		SellerRegistrationResponse response = sellerApprovalService.rejectSeller(12L);

		verify(sellerRegistrationRepository).save(registration);
		ArgumentCaptor<SellerRegistrationRejectedEvent> captor = ArgumentCaptor.forClass(SellerRegistrationRejectedEvent.class);
		verify(sellerOutboxService).enqueueSellerRejected(captor.capture());
		assertThat(captor.getValue().registrationId()).isEqualTo(200L);
		assertThat(captor.getValue().memberId()).isEqualTo(12L);
		assertThat(captor.getValue().email()).isEqualTo("seller12@example.com");
		assertThat(response.status()).isEqualTo(SellerRegistrationStatus.REJECTED);
	}

	@Test
	void rejectSeller_skipsOutboxWhenAlreadyRejected() {

		SellerRegistration registration = SellerRegistration.create(13L, "store-13", "biz-13", "010-0000-0003");
		ReflectionTestUtils.setField(registration, "id", 300L);
		registration.reject();

		when(sellerRegistrationRepository.findByMemberId(13L)).thenReturn(Optional.of(registration));

		sellerApprovalService.rejectSeller(13L);

		verify(sellerRegistrationRepository, never()).save(any(SellerRegistration.class));
		verify(sellerOutboxService, never()).enqueueSellerRejected(any(SellerRegistrationRejectedEvent.class));
	}
}
