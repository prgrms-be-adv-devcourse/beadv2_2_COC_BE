package com.coc.modi.review.application;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter
@ConfigurationProperties(prefix = "review.summary")
public class ReviewSummaryPolicyProperties {

	@NotNull
	@Min(1)
	private Integer minTotalCount;

	@NotNull
	@Min(1)
	private Integer minNewCount;

	@NotNull
	@Min(1)
	private Integer recentLimit;

	@NotNull
	@Min(1)
	private Integer maxLength;
}
