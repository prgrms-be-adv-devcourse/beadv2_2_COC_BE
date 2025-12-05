package com.coc.modi.product.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {

    // 단순 ACTIVE 상품 목록
    Page<ProductDocument> findByStatus(String status, Pageable pageable);

    // 이름/설명에 keyword 포함 검색 (간단 버전)
    Page<ProductDocument> findByStatusAndNameContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
            String status1, String nameKeyword,
            String status2, String descKeyword,
            Pageable pageable
    );
}
