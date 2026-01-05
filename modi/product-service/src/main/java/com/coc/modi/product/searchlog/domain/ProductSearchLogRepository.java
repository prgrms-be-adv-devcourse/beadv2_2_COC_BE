package com.coc.modi.product.searchlog.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductSearchLogRepository extends JpaRepository<ProductSearchLog, Long> {

	List<ProductSearchLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

	List<ProductSearchLog> findByCreatedAtGreaterThanEqual(LocalDateTime start);

	List<ProductSearchLog> findByCreatedAtLessThanEqual(LocalDateTime end);
}
