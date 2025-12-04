package com.coc.modi.product.presentation.dto;

import com.coc.modi.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductBulkResponseDto {

    private Long productId;
    private Long sellerId;
    private Long price;   // BigDecimal 쓰고 싶으면 타입만 바꾸면 됨
    private String status;

    public static ProductBulkResponseDto from(Product product) {
        return new ProductBulkResponseDto(
                product.getId(),
                product.getSellerId(),
                product.getPricePerDay().longValue(),
                product.getStatus().name()
        );
    }
}
