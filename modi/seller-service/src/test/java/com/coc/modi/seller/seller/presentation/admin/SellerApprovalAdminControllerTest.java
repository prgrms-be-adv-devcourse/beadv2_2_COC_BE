package com.coc.modi.seller.seller.presentation.admin;

import com.coc.modi.seller.seller.application.SellerApprovalService;
import com.coc.modi.seller.seller.application.dto.SellerRegistrationResponse;
import com.coc.modi.seller.seller.registration.domain.SellerRegistrationStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"spring.config.name=application-test",
		"management.health.redis.enabled=false",
		"management.health.redis-reactive.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SellerApprovalAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SellerApprovalService sellerApprovalService;

	@MockBean
	private RedisConnectionFactory redisConnectionFactory;

	@MockBean
	private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

	@MockBean
	private RedisMessageListenerContainer redisMessageListenerContainer;

	@Test
	void approveSeller_deniesNonAdmin() throws Exception {

		mockMvc.perform(patch("/api/admin/sellers/{memberId}/approve", 1L)
						.header("X-Member-Id", "10")
						.header("X-Roles", "MEMBER"))
				.andExpect(status().isForbidden());

		verifyNoInteractions(sellerApprovalService);
	}

	@Test
	void approveSeller_allowsAdmin() throws Exception {

		when(sellerApprovalService.approveSeller(1L, 10L))
				.thenReturn(stubResponse(100L, 1L, SellerRegistrationStatus.APPROVED, 10L));

		mockMvc.perform(patch("/api/admin/sellers/{memberId}/approve", 1L)
						.header("X-Member-Id", "10")
						.header("X-Roles", "ADMIN"))
				.andExpect(status().isOk());

		verify(sellerApprovalService).approveSeller(1L, 10L);
	}

	@Test
	void rejectSeller_deniesNonAdmin() throws Exception {

		mockMvc.perform(patch("/api/admin/sellers/{memberId}/reject", 2L)
						.header("X-Member-Id", "11")
						.header("X-Roles", "SELLER"))
				.andExpect(status().isForbidden());

		verifyNoInteractions(sellerApprovalService);
	}

	@Test
	void rejectSeller_allowsAdmin() throws Exception {

		when(sellerApprovalService.rejectSeller(2L))
				.thenReturn(stubResponse(200L, 2L, SellerRegistrationStatus.REJECTED, null));

		mockMvc.perform(patch("/api/admin/sellers/{memberId}/reject", 2L)
						.header("X-Member-Id", "11")
						.header("X-Roles", "ADMIN"))
				.andExpect(status().isOk());

		verify(sellerApprovalService).rejectSeller(2L);
	}

	private SellerRegistrationResponse stubResponse(Long registrationId,
													Long memberId,
													SellerRegistrationStatus status,
													Long approvedBy) {

		return new SellerRegistrationResponse(
				registrationId,
				memberId,
				"store",
				"biz",
				"010-0000-0000",
				status,
				approvedBy
		);
	}
}
