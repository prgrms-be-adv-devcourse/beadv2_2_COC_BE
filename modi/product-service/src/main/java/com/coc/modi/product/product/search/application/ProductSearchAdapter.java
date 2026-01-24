package com.coc.modi.product.product.search.application;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.coc.modi.product.product.application.dto.ProductListResponse;
import com.coc.modi.product.product.application.dto.ProductScrollResponse;
import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.product.application.dto.RentalResponse;
import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductImageRepository;
import com.coc.modi.product.product.infrastructure.client.RentalAvailabilityClient;
import com.coc.modi.product.product.presentation.dto.RentalRequest;
import com.coc.modi.product.product.search.domain.ProductSortType;
import com.coc.modi.product.product.search.infrastructure.ProductSearchQueryRepository;
import com.coc.modi.product.searchlog.application.KeywordNormalizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.coc.modi.product.support.ProductSearchSizeNormalizer;

import static net.logstash.logback.argument.StructuredArguments.kv;
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductSearchAdapter implements ProductSearchPort {

	private static final ZoneOffset CURSOR_ZONE = ZoneOffset.ofHours(9);

	private final ProductSearchQueryRepository searchQueryRepository;
	private final ProductImageRepository productImageRepository;
	private final RentalAvailabilityClient rentalAvailabilityClient;
	private final KeywordNormalizationService keywordNormalizationService;

	@Override
	@Transactional(readOnly = true)
	public ProductScrollResponse searchProducts(ProductSearchCondition condition,
												String cursor,
												int size,
												ProductSortType sortType) {
		ProductSortType effectiveSortType = sortType != null ? sortType : ProductSortType.LATEST;
		int resolvedSize = ProductSearchSizeNormalizer.normalize(size);

		List<ProductListResponse> items = new ArrayList<>();
		String currentCursor = cursor;
		boolean hasMore = true;
		int safetyLimit = 5;

		List<Product> lastBatch = List.of();

		String normalizedKeyword = keywordNormalizationService.normalizeForSearch(
				condition != null ? condition.keyword() : null);

		while (items.size() < resolvedSize && hasMore && safetyLimit-- > 0) {
			List<Product> products = searchQueryRepository.search(
					condition,
					normalizedKeyword,
					currentCursor,
					resolvedSize,
					effectiveSortType
			);
			if (products.isEmpty()) {
				hasMore = false;
				break;
			}

			lastBatch = products;
			List<Product> availableProducts = products;

			if (condition != null && condition.hasRentalPeriod()) {
				List<Long> productIds = products.stream()
						.map(Product::getId)
						.toList();

				if (!productIds.isEmpty()) {
					try {
						RentalRequest request = new RentalRequest(condition.startDate(), condition.endDate(), productIds);
						RentalResponse rentalResponse = rentalAvailabilityClient.unavailableProducts(request);

						Set<Long> unavailableIds = new HashSet<>(
								Optional.ofNullable(rentalResponse)
										.map(RentalResponse::unavailableProductIds)
										.orElseGet(List::of)
						);

						availableProducts = products.stream()
								.filter(product -> !unavailableIds.contains(product.getId()))
								.toList();
					} catch (Exception e) {
						log.warn("rental_availability_lookup_failed",
								kv("log_type", "service"),
								kv("error.type", "RENTAL_AVAILABILITY_LOOKUP_FAILED"),
								kv("product.search.start_date", condition.startDate()),
								kv("product.search.end_date", condition.endDate()),
								kv("product.search.product_count", productIds.size()),
								kv("exception.class", e.getClass().getName()),
								e);
					}
				}
			}

			Map<Long, String> thumbnailUrlMap = productImageRepository.findUrlMapByIds(
					availableProducts.stream()
							.map(Product::getThumbnailImageId)
							.toList()
			);

			items.addAll(availableProducts.stream()
					.map(product -> ProductListResponse.fromProduct(
							product,
							thumbnailUrlMap.get(product.getThumbnailImageId())))
					.toList());

			currentCursor = buildNextCursor(products, effectiveSortType);
			hasMore = products.size() == resolvedSize && currentCursor != null;
		}

		if (items.isEmpty()) {
			return new ProductScrollResponse(List.of(), null, false);
		}

		if (items.size() > resolvedSize) {
			items = items.subList(0, resolvedSize);
		}

		String nextCursor = (hasMore && !lastBatch.isEmpty())
				? currentCursor
				: null;

		return new ProductScrollResponse(items, nextCursor, hasMore);
	}

	private String buildNextCursor(List<Product> products, ProductSortType sortType) {
		return switch (sortType) {
			case LATEST, OLDEST -> {
				Product target = null;
				for (int i = products.size() - 1; i >= 0; i--) {
					if (products.get(i).getCreatedAt() != null) {
						target = products.get(i);
						break;
					}
				}

				if (target == null) {
					yield null;
				}

				long epochMillis = target.getCreatedAt()
						.atOffset(CURSOR_ZONE)
						.toInstant()
						.toEpochMilli();

				String rawCursor = epochMillis + "|" + target.getId();
				yield Base64.getUrlEncoder()
						.encodeToString(rawCursor.getBytes(StandardCharsets.UTF_8));
			}
			case PRICE_HIGH, PRICE_LOW -> {
				Product target = null;
				for (int i = products.size() - 1; i >= 0; i--) {
					if (products.get(i).getPricePerDay() != null) {
						target = products.get(i);
						break;
					}
				}

				if (target == null) {
					yield null;
				}

				BigDecimal price = target.getPricePerDay();
				Long id = target.getId();
				yield price.toPlainString() + ":" + id;
			}
		};
	}
}
