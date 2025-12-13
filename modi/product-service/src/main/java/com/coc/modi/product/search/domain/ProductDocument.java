package com.coc.modi.product.search.domain;

import com.coc.modi.product.product.domain.Product;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@NoArgsConstructor
@Document(indexName = "products", createIndex = false) // ES 인덱스 이름
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
	
	@Field(type = FieldType.Date, format = DateFormat.strict_date_optional_time_nanos)
	private OffsetDateTime createdAt;
	
	public ProductDocument(Long id,
						   String name,
						   String description,
						   BigDecimal pricePerDay,
						   String status,
						   Long sellerId,
						   String category,
						   String thumbnailUrl,
						   OffsetDateTime createdAt) {
		
		this.id = id;
		this.name = name;
		this.description = description;
		this.pricePerDay = pricePerDay;
		this.status = status;
		this.sellerId = sellerId;
		this.category = category;
		this.thumbnailUrl = thumbnailUrl;
		this.createdAt = createdAt;
	}
	
	public static ProductDocument from(Product product, String thumbnailUrl) {
		
		OffsetDateTime createdAt = null;
		if (product.getCreatedAt() != null) {
			createdAt = product.getCreatedAt().atOffset(ZoneOffset.of("+09:00"));
		}
		
		return new ProductDocument(
				product.getId(),
				product.getName(),
				product.getDescription(),
				product.getPricePerDay(),
				product.getStatus().name(),
				product.getSellerId(),
				product.getCategory().name(),
				thumbnailUrl,
				createdAt
		);
	}
}
