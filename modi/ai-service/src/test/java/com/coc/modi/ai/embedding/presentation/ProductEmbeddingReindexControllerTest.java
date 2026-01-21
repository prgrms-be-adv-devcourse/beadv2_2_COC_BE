package com.coc.modi.ai.embedding.presentation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coc.modi.ai.embedding.application.ProductEmbeddingReindexService;
import com.coc.modi.common.auth.SecurityConfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProductEmbeddingReindexController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ProductEmbeddingReindexControllerTest {

    static {
        System.setProperty("spring.config.location", "classpath:/application-test.yml");
    }

    private static final String MEMBER_ID_HEADER = "X-Member-Id";
    private static final String ROLES_HEADER = "X-Roles";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductEmbeddingReindexService productEmbeddingReindexService;

    @Test
    void reindexEmbeddings_returnsCount() throws Exception {
        when(productEmbeddingReindexService.reindexMissing()).thenReturn(3);

        mockMvc.perform(post("/api/ai/embeddings/reindex")
                        .header(MEMBER_ID_HEADER, "1")
                        .header(ROLES_HEADER, "MEMBER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(3));

        verify(productEmbeddingReindexService).reindexMissing();
    }

    @Test
    void reindexEmbedding_returnsResult() throws Exception {
        when(productEmbeddingReindexService.reindexMissingOne(10L)).thenReturn(true);

        mockMvc.perform(post("/api/ai/10/embedding")
                        .header(MEMBER_ID_HEADER, "1")
                        .header(ROLES_HEADER, "MEMBER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(productEmbeddingReindexService).reindexMissingOne(10L);
    }
}
