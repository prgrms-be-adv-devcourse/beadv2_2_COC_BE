package com.coc.modi.ai.embedding.domain;

import java.util.Map;

public record ProductEmbeddingTarget(
        Long productId,
        String name,
        String description,
        String category,
        Map<String, String> specs,
        String status
) {
}
