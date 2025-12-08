package com.coc.modi.product.search;

import com.coc.modi.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.domain.ProductCategory;
import com.coc.modi.product.domain.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductSearchQueryRepository {

    private final ElasticsearchOperations operations;

    public Page<ProductDocument> search(ProductSearchCondition cond, Pageable pageable) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            // status ACTIVE 필터
                            b. filter(f -> f.term(t -> t.field("status").value(ProductStatus.ACTIVE.name())));
                            
                            // 키워드 필터(name, description)
                            if (StringUtils.hasText(cond.keyword())) {
                                b.must(m -> m
                                        .multiMatch(mm -> mm.fields("name", "description").query(cond.keyword())));
                            }
                            
                            // 카테고리 필터
                            if(cond.category() != null) {
                                ProductCategory category = cond.category();
                                b.filter(f -> f.term(t -> t.field("category").value(category.name())));
                            }
                            
                            // 가격 범위 필터
                            if(cond.minPrice() != null || cond.maxPrice() != null) {
                                b.filter(f -> f.range(r -> r.number(n -> {
                                    n.field("pricePerDay");

                                    if(cond.minPrice() != null) {
                                        n.gte(cond.minPrice().doubleValue());
                                    }
                                    if(cond.maxPrice() != null) {
                                        n.lte(cond.maxPrice().doubleValue());
                                    }

                                    return n;
                                })));
                            }
                            
                            // 판매자 필터
                            if(cond.sellerId() != null) {
                                b.filter(f -> f
                                        .term(t -> t.field("sellerId").value(cond.sellerId())));
                            }
                            
                            return b;
                        }))
                .withPageable(pageable).build();

        SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);

        List<ProductDocument> content = hits.getSearchHits().stream()
                .map(SearchHit::getContent).toList();

        long total = hits.getTotalHits();

        return new PageImpl<>(content, pageable, total);
    }
}
