package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.application.port.RentalPort;
import com.coc.modi.seller.infrastructure.client.rental.dto.RentalItemInfo;
import com.coc.modi.seller.seller.domain.SellerRepository;
import com.coc.modi.seller.settlement.application.SettlementAggregationService;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class SettlementBatchJobConfig {
	
	private final SettlementBatchJobListener jobListener;
	private final SettlementBatchStepListener stepListener;
	private final SettlementAggregationService settlementAggregationService;
	private final RentalPort rentalPort;
	private final SellerRepository sellerRepository;
	
	@Value("${settlement.batch.chunk-size:50}")
	private int chunkSize;
	
	@Bean
	public Job settlementAggregationJob(JobRepository jobRepository,
										Step settlementAggregationStep) {
		
		return new JobBuilder("settlementAggregationJob", jobRepository)
				.listener(jobListener)
				.start(settlementAggregationStep)
				.build();
	}
	
	@Bean
	public Step settlementAggregationStep(JobRepository jobRepository,
										  PlatformTransactionManager transactionManager,
										  SettlementRentalItemReader settlementRentalItemReader,
										  SettlementAggregationProcessor settlementAggregationProcessor,
										  SettlementAggregationWriter settlementAggregationWriter,
										  SettlementSkipListener settlementSkipListener) {
		
		return new StepBuilder("settlementAggregationStep", jobRepository)
				.<RentalItemInfo, SettlementAggregationItem>chunk(chunkSize, transactionManager)
				.reader(settlementRentalItemReader)
				.processor(settlementAggregationProcessor)
				.writer(settlementAggregationWriter)
				.faultTolerant()
				.retryLimit(3)
				.retry(RetryableException.class)
				.retry(FeignException.ServiceUnavailable.class)
				.retry(FeignException.GatewayTimeout.class)
				.backOffPolicy(settlementRetryBackOffPolicy())
				.skip(FeignException.BadRequest.class)
				.skip(FeignException.NotFound.class)
				.skipLimit(50)
				.listener(settlementSkipListener)
				.listener(stepListener)
				.build();
	}
	
	@Bean
	@StepScope
	public SettlementRentalItemReader settlementRentalItemReader(
			@Value("#{jobParameters['periodYm']}") String periodYm,
			@Value("#{jobParameters['startDate']}") String startDate,
			@Value("#{jobParameters['endDate']}") String endDate,
			@Value("#{jobParameters['sellerId']}") Long sellerId,
			@Value("#{jobParameters['pageSize']}") Integer pageSize
	) {
		
		if (periodYm == null || periodYm.isBlank()) {
			throw new IllegalArgumentException("periodYm is required");
		}
		return new SettlementRentalItemReader(
				rentalPort,
				sellerRepository,
				periodYm,
				startDate,
				endDate,
				sellerId,
				pageSize
		);
	}
	
	@Bean
	@StepScope
	public SettlementAggregationProcessor settlementAggregationProcessor(
			@Value("#{jobParameters['periodYm']}") String periodYm
	) {
		
		return new SettlementAggregationProcessor(periodYm);
	}
	
	@Bean
	@StepScope
	public SettlementAggregationWriter settlementAggregationWriter() {
		return new SettlementAggregationWriter(settlementAggregationService);
	}
	
	@Bean
	public SettlementSkipListener settlementSkipListener() {
		return new SettlementSkipListener();
	}
	
	@Bean
	public BackOffPolicy settlementRetryBackOffPolicy() {
		ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
		backOffPolicy.setInitialInterval(1000L);
		backOffPolicy.setMultiplier(2.0);
		backOffPolicy.setMaxInterval(4000L);
		return backOffPolicy;
	}
}
