package com.coc.modi.ai.moderation.domain;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductModerationResultRepository extends JpaRepository<ProductModerationResult, UUID> {
}
