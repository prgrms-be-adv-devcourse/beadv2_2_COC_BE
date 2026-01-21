package com.coc.modi.member.admin.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coc.modi.common.auth.SecurityConfig;
import com.coc.modi.member.admin.application.AdminMemberService;
import com.coc.modi.member.admin.application.dto.AdminMemberCreateCommand;
import com.coc.modi.member.admin.application.dto.AdminMemberCreateResponse;
import com.coc.modi.member.admin.presentation.dto.AdminMemberCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
		value = AdminMemberController.class,
		properties = {
				"spring.config.name=application-test",
				"spring.cloud.config.enabled=false",
				"spring.cloud.config.import-check.enabled=false",
				"spring.cloud.config.discovery.enabled=false",
				"spring.cloud.discovery.enabled=false",
				"eureka.client.enabled=false"
		}
)
@Import(SecurityConfig.class)
class AdminMemberControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AdminMemberService adminMemberService;

	@MockBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Test
	void create_admin_returns_ok_response() throws Exception {

		AdminMemberCreateRequest request = new AdminMemberCreateRequest(
				"admin@example.com",
				"Password!1",
				"Admin",
				"010-1234-5678"
		);

		AdminMemberCreateResponse response = new AdminMemberCreateResponse(
				10L,
				"admin@example.com",
				"ADMIN"
		);

		when(adminMemberService.createAdmin(any(AdminMemberCreateCommand.class), eq(1L)))
				.thenReturn(response);

		mockMvc.perform(post("/api/admin/members")
						.header("X-Member-Id", "1")
						.header("X-Roles", "ADMIN")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.memberId").value(10))
				.andExpect(jsonPath("$.data.email").value("admin@example.com"))
				.andExpect(jsonPath("$.data.role").value("ADMIN"));

		verify(adminMemberService).createAdmin(any(AdminMemberCreateCommand.class), eq(1L));
	}

	@Test
	void create_admin_rejects_unauthenticated_request() throws Exception {

		AdminMemberCreateRequest request = new AdminMemberCreateRequest(
				"admin@example.com",
				"Password!1",
				"Admin",
				"010-1234-5678"
		);

		mockMvc.perform(post("/api/admin/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());
	}

	@Test
	void create_admin_rejects_non_admin_role() throws Exception {

		AdminMemberCreateRequest request = new AdminMemberCreateRequest(
				"admin@example.com",
				"Password!1",
				"Admin",
				"010-1234-5678"
		);

		mockMvc.perform(post("/api/admin/members")
						.header("X-Member-Id", "2")
						.header("X-Roles", "SELLER")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());
	}
}
