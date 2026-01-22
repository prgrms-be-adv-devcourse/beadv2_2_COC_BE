package com.coc.modi.delivery.delivery.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import com.coc.modi.delivery.delivery.application.dto.DeliveryCreateCommand;
import com.coc.modi.delivery.delivery.application.dto.DeliveryCreateResponse;
import com.coc.modi.delivery.delivery.application.dto.DeliveryDetailResponse;
import com.coc.modi.delivery.delivery.domain.Delivery;
import com.coc.modi.delivery.delivery.domain.DeliveryRepository;
import com.coc.modi.delivery.delivery.domain.DeliveryStatus;
import com.coc.modi.delivery.delivery.infrastructure.TrackingResult;
import com.coc.modi.delivery.delivery.infrastructure.client.rental.RentalInternalFeignClient;
import com.coc.modi.delivery.delivery.infrastructure.client.rental.dto.RentalItemSellerResponse;
import com.coc.modi.delivery.delivery.infrastructure.client.seller.SellerInternalFeignClient;
import com.coc.modi.delivery.delivery.infrastructure.client.seller.dto.SellerIdResponse;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

	@Mock
	private DeliveryRepository deliveryRepository;

	@Mock
	private RentalInternalFeignClient rentalInternalFeignClient;

	@Mock
	private SellerInternalFeignClient sellerInternalFeignClient;

	@InjectMocks
	private DeliveryService deliveryService;

	@Test
	void create_delivery_creates_when_not_exists() {
		Long memberId = 10L;
		Long sellerId = 20L;
		DeliveryCreateCommand command = new DeliveryCreateCommand(123L, "CJ", "1234567890");
		Delivery saved = Delivery.create(123L, "CJ", "1234567890");
		ReflectionTestUtils.setField(saved, "id", 1L);

		when(rentalInternalFeignClient.getRentalItemSeller(123L))
				.thenReturn(new RentalItemSellerResponse(123L, sellerId, memberId));
		when(sellerInternalFeignClient.getSellerByMember(memberId))
				.thenReturn(new SellerIdResponse(sellerId, memberId));
		when(deliveryRepository.findByRentalItemId(123L)).thenReturn(Optional.empty());
		when(deliveryRepository.save(any(Delivery.class))).thenReturn(saved);

		DeliveryCreateResponse response = deliveryService.createDelivery(command, memberId);

		assertThat(response.deliveryId()).isEqualTo(1L);
		assertThat(response.rentalItemId()).isEqualTo(123L);
		assertThat(response.carrierCode()).isEqualTo("CJ");
		assertThat(response.trackingNumber()).isEqualTo("1234567890");
		assertThat(response.status()).isEqualTo(DeliveryStatus.REGISTERED);

		verify(rentalInternalFeignClient).getRentalItemSeller(123L);
		verify(sellerInternalFeignClient).getSellerByMember(memberId);
		verify(deliveryRepository).findByRentalItemId(123L);
		verify(deliveryRepository).save(any(Delivery.class));
	}

	@Test
	void create_delivery_throws_conflict_when_exists() {
		Long memberId = 10L;
		Long sellerId = 20L;
		DeliveryCreateCommand command = new DeliveryCreateCommand(123L, "NEW", "9999999999");
		Delivery existing = Delivery.create(123L, "OLD", "1111111111");

		when(rentalInternalFeignClient.getRentalItemSeller(123L))
				.thenReturn(new RentalItemSellerResponse(123L, sellerId, memberId));
		when(sellerInternalFeignClient.getSellerByMember(memberId))
				.thenReturn(new SellerIdResponse(sellerId, memberId));
		when(deliveryRepository.findByRentalItemId(123L)).thenReturn(Optional.of(existing));

		org.junit.jupiter.api.Assertions.assertThrows(
				com.coc.modi.delivery.delivery.exception.DeliveryConflictException.class,
				() -> deliveryService.createDelivery(command, memberId)
		);

		verify(rentalInternalFeignClient).getRentalItemSeller(123L);
		verify(sellerInternalFeignClient).getSellerByMember(memberId);
		verify(deliveryRepository).findByRentalItemId(123L);
	}

	@Test
	void update_delivery_updates_when_exists() {
		Long memberId = 10L;
		Long sellerId = 20L;
		com.coc.modi.delivery.delivery.application.dto.DeliveryUpdateCommand command =
				new com.coc.modi.delivery.delivery.application.dto.DeliveryUpdateCommand("NEW", "9999999999");
		Delivery existing = Delivery.create(123L, "OLD", "1111111111");
		ReflectionTestUtils.setField(existing, "id", 1L);
		existing.applyTrackingResult(DeliveryStatus.DELIVERED, new TrackingResult("DELIVERED", "done", true));
		existing.markTrackedNow();

		when(rentalInternalFeignClient.getRentalItemSeller(123L))
				.thenReturn(new RentalItemSellerResponse(123L, sellerId, memberId));
		when(sellerInternalFeignClient.getSellerByMember(memberId))
				.thenReturn(new SellerIdResponse(sellerId, memberId));
		when(deliveryRepository.findByRentalItemId(123L)).thenReturn(Optional.of(existing));

		DeliveryDetailResponse response = deliveryService.updateDeliveryByRentalItemId(123L, command, memberId);

		assertThat(existing.getCarrierCode()).isEqualTo("NEW");
		assertThat(existing.getTrackingNumber()).isEqualTo("9999999999");
		assertThat(existing.getStatus()).isEqualTo(DeliveryStatus.REGISTERED);
		assertThat(existing.getStatusRaw()).isNull();
		assertThat(existing.getLastTrackedAt()).isNull();

		assertThat(response.deliveryId()).isEqualTo(1L);
		assertThat(response.status()).isEqualTo(DeliveryStatus.REGISTERED);

		verify(rentalInternalFeignClient).getRentalItemSeller(123L);
		verify(sellerInternalFeignClient).getSellerByMember(memberId);
		verify(deliveryRepository).findByRentalItemId(123L);
	}
}
