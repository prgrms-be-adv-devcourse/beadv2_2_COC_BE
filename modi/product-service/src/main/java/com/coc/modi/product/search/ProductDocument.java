package com.coc.modi.product.search;

import com.coc.modi.product.domain.Product;
import com.coc.modi.product.domain.ProductStatus;
import com.coc.modi.product.domain.ProductCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Document(indexName = "products") // ES 인덱스 이름
public class ProductDocument {

    @Id
    private Long id;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Double)
    private BigDecimal pricePerDay;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Long)
    private Long sellerId;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String thumbnailUrl;

    public ProductDocument(Long id,
                           String name,
                           String description,
                           BigDecimal pricePerDay,
                           String status,
                           Long sellerId,
                           String category,
                           String thumbnailUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pricePerDay = pricePerDay;
        this.status = status;
        this.sellerId = sellerId;
        this.category = category;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static ProductDocument from(Product product, String thumbnailUrl) {
        return new ProductDocument(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPricePerDay(),
                product.getStatus().name(),
                product.getSellerId(),
                product.getCategory().name(),
                thumbnailUrl
        );
    }

    public ProductStatus toStatusEnum() {
        return ProductStatus.valueOf(this.status);
    }

    public ProductCategory toCategoryEnum() {
        return ProductCategory.valueOf(this.category);
    }
}
