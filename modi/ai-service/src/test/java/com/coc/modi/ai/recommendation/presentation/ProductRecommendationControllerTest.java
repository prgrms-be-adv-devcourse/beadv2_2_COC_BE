package com.coc.modi.ai.recommendation.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coc.modi.ai.chat.application.ChatService;
import com.coc.modi.ai.chat.domain.ChatResult;
import com.coc.modi.ai.recommendation.application.ProductDescriptionService;
import com.coc.modi.ai.recommendation.application.ProductRecommendationService;
import com.coc.modi.ai.recommendation.presentation.dto.ProductRecommendationRequest;
import com.coc.modi.ai.recommendation.presentation.dto.ProductRecommendationResponse;
import com.coc.modi.common.auth.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProductRecommendationController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ProductRecommendationControllerTest {

    static {
        System.setProperty("spring.config.location", "classpath:/application-test.yml");
    }

    private static final String MEMBER_ID_HEADER = "X-Member-Id";
    private static final String ROLES_HEADER = "X-Roles";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductRecommendationService productRecommendationService;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private ProductDescriptionService productDescriptionService;

    @Test
    void recommendProducts_returnsResponse() throws Exception {
        List<ProductRecommendationResponse.Item> items = List.of(
                new ProductRecommendationResponse.Item(
                        1L,
                        "product-name",
                        "category",
                        Map.of("color", "red"),
                        "ACTIVE",
                        0.12
                )
        );
        String expectedMessage = "recommended";

        when(productRecommendationService.recommend(any())).thenReturn(items);
        when(productRecommendationService.buildRecommendationMessage(any(ProductRecommendationRequest.class), eq(items)))
                .thenReturn(expectedMessage);

        Map<String, Object> request = Map.of(
                "productId", 1,
                "query", "camping",
                "categories", List.of("outdoor"),
                "size", 2
        );

        mockMvc.perform(post("/api/ai/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(MEMBER_ID_HEADER, "1")
                        .header(ROLES_HEADER, "MEMBER")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.message").value(expectedMessage))
                .andExpect(jsonPath("$.data.items[0].productId").value(1))
                .andExpect(jsonPath("$.data.items[0].specs.color").value("red"));

        verify(productRecommendationService).recommend(any());
        verify(productRecommendationService).buildRecommendationMessage(any(ProductRecommendationRequest.class), eq(items));
    }

    @Test
    void chatTest_returnsContent() throws Exception {
        when(chatService.chat("hello"))
                .thenReturn(new ChatResult("chat-response", Map.of()));

        Map<String, Object> request = Map.of("message", "hello");

        mockMvc.perform(post("/api/ai/ai/chat-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(MEMBER_ID_HEADER, "1")
                        .header(ROLES_HEADER, "MEMBER")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("chat-response"));

        verify(chatService).chat("hello");
    }

    @Test
    void recommendRecent_returnsItems() throws Exception {
        List<ProductRecommendationResponse.Item> items = List.of(
                new ProductRecommendationResponse.Item(
                        10L,
                        "recent-product",
                        "category",
                        Map.of("size", "M"),
                        "ACTIVE",
                        0.34
                )
        );

        when(productRecommendationService.recommendRecent(1L, 5)).thenReturn(items);

        mockMvc.perform(get("/api/ai/recommendations/recent")
                        .param("size", "5")
                        .header(MEMBER_ID_HEADER, "1")
                        .header(ROLES_HEADER, "MEMBER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].productId").value(10))
                .andExpect(jsonPath("$.data[0].specs.size").value("M"));

        verify(productRecommendationService).recommendRecent(1L, 5);
    }

    @Test
    void recommendProducts_rejectsInvalidRequest() throws Exception {
        Map<String, Object> request = Map.of(
                "productId", 0,
                "size", 0
        );

        mockMvc.perform(post("/api/ai/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(MEMBER_ID_HEADER, "1")
                        .header(ROLES_HEADER, "MEMBER")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chatTest_rejectsBlankMessage() throws Exception {
        Map<String, Object> request = Map.of("message", " ");

        mockMvc.perform(post("/api/ai/ai/chat-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(MEMBER_ID_HEADER, "1")
                        .header(ROLES_HEADER, "MEMBER")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recommendDescription_returns_content() throws Exception {
        when(productDescriptionService.recommendDescription(any()))
                .thenReturn("추천 설명");

        Map<String, Object> request = Map.of(
                "productName", "아이폰15",
                "category", "MOBILE",
                "specs", Map.of("storage", "256GB")
        );

        mockMvc.perform(post("/api/ai/descriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(MEMBER_ID_HEADER, "1")
                        .header(ROLES_HEADER, "MEMBER")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("추천 설명"));

        verify(productDescriptionService).recommendDescription(any());
    }
}
