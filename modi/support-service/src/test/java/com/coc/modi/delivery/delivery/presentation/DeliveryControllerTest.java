package com.coc.modi.delivery.delivery.presentation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.coc.modi.common.auth.SecurityConfig;
import com.coc.modi.delivery.delivery.application.DeliveryService;
import com.coc.modi.delivery.delivery.application.dto.DeliveryCreateResponse;
import com.coc.modi.delivery.delivery.application.dto.DeliveryDetailResponse;
import com.coc.modi.delivery.delivery.domain.DeliveryStatus;
import com.coc.modi.delivery.delivery.exception.DeliveryNotFoundException;
import com.coc.modi.delivery.exception.GlobalExceptionHandler;

@WebMvcTest(controllers = DeliveryController.class)
@Import({SecurityConfig.class, DeliveryController.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class DeliveryControllerTest {

	static {
		System.setProperty("spring.config.location", "classpath:/application-test.yml");
	}

	private static final String MEMBER_ID_HEADER = "X-Member-Id";
	private static final String ROLES_HEADER = "X-Roles";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DeliveryService deliveryService;

	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Test
	void create_delivery_returns_created() throws Exception {
		DeliveryCreateResponse response = new DeliveryCreateResponse(
				1L,
				123L,
				"CJ",
				"1234567890",
				DeliveryStatus.REGISTERED
		);

		when(deliveryService.createDelivery(org.mockito.ArgumentMatchers.any(),
											org.mockito.ArgumentMatchers.anyLong()))
				.thenReturn(response);

		String payload = "{\"rentalItemId\":123,\"carrierCode\":\"CJ\",\"trackingNumber\":\"1234567890\"}";

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/deliveries")
						.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
						.header(MEMBER_ID_HEADER, "1")
						.header(ROLES_HEADER, "MEMBER")
						.content(payload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.code").value("OK"))
				.andExpect(jsonPath("$.data.deliveryId").value(1))
				.andExpect(jsonPath("$.data.rentalItemId").value(123))
				.andExpect(jsonPath("$.data.trackingNumber").value("1234567890"))
				.andExpect(jsonPath("$.data.status").value("REGISTERED"));

		verify(deliveryService).createDelivery(
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.anyLong());
	}

	@Test
	void get_delivery_by_rental_item_id_returns_detail() throws Exception {
		DeliveryDetailResponse response = new DeliveryDetailResponse(
				1L,
				123L,
				"CJ",
				"1234567890",
				DeliveryStatus.IN_TRANSIT,
				"IN_TRANSIT",
				LocalDateTime.of(2026, 1, 16, 10, 0),
				LocalDateTime.of(2026, 1, 16, 11, 0)
		);

		when(deliveryService.getDeliveryByRentalItemId(org.mockito.ArgumentMatchers.eq(123L),
													org.mockito.ArgumentMatchers.anyLong()))
				.thenReturn(response);

		mockMvc.perform(get("/api/deliveries/rental-items/{rentalItemId}", 123L)
						.header(MEMBER_ID_HEADER, "1")
						.header(ROLES_HEADER, "MEMBER"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.code").value("OK"))
				.andExpect(jsonPath("$.data.deliveryId").value(1))
				.andExpect(jsonPath("$.data.rentalItemId").value(123))
				.andExpect(jsonPath("$.data.trackingNumber").value("1234567890"))
				.andExpect(jsonPath("$.data.status").value("IN_TRANSIT"));

		verify(deliveryService).getDeliveryByRentalItemId(
				org.mockito.ArgumentMatchers.eq(123L),
				org.mockito.ArgumentMatchers.anyLong());
	}

	@Test
	void update_delivery_by_rental_item_id_returns_detail() throws Exception {
		DeliveryDetailResponse response = new DeliveryDetailResponse(
				1L,
				123L,
				"NEW",
				"9999999999",
				DeliveryStatus.REGISTERED,
				null,
				LocalDateTime.of(2026, 1, 16, 10, 0),
				LocalDateTime.of(2026, 1, 16, 11, 0)
		);

		when(deliveryService.updateDeliveryByRentalItemId(org.mockito.ArgumentMatchers.eq(123L),
													org.mockito.ArgumentMatchers.any(),
													org.mockito.ArgumentMatchers.anyLong()))
				.thenReturn(response);

		String payload = "{\"carrierCode\":\"NEW\",\"trackingNumber\":\"9999999999\"}";

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.patch("/api/deliveries/{rentalItemId}", 123L)
						.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
						.header(MEMBER_ID_HEADER, "1")
						.header(ROLES_HEADER, "MEMBER")
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.code").value("OK"))
				.andExpect(jsonPath("$.data.deliveryId").value(1))
				.andExpect(jsonPath("$.data.rentalItemId").value(123))
				.andExpect(jsonPath("$.data.trackingNumber").value("9999999999"))
				.andExpect(jsonPath("$.data.status").value("REGISTERED"));

		verify(deliveryService).updateDeliveryByRentalItemId(
				org.mockito.ArgumentMatchers.eq(123L),
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.anyLong());
	}

	@Test
	void get_delivery_by_rental_item_id_returns_not_found() throws Exception {
		when(deliveryService.getDeliveryByRentalItemId(org.mockito.ArgumentMatchers.eq(999L),
													org.mockito.ArgumentMatchers.anyLong()))
				.thenThrow(new DeliveryNotFoundException("rentalItemId", 999L));

		mockMvc.perform(get("/api/deliveries/rental-items/{rentalItemId}", 999L)
						.header(MEMBER_ID_HEADER, "1")
						.header(ROLES_HEADER, "MEMBER"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("DELIVERY-404"));

		verify(deliveryService).getDeliveryByRentalItemId(
				org.mockito.ArgumentMatchers.eq(999L),
				org.mockito.ArgumentMatchers.anyLong());
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestApplication {
	}
}
