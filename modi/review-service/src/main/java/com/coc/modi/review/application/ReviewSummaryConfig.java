package com.coc.modi.review.application;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ReviewSummaryPolicyProperties.class)
public class ReviewSummaryConfig{
}
