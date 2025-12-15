package com.coc.modi.product.product.infrastructure.batch;

import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductStatus;
import com.coc.modi.product.search.application.ProductIndexService;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ProductReindexJobConfig {
	
	private final EntityManagerFactory entityManagerFactory;
	private final ProductIndexService productIndexService;
	
	private static final int CHUNK_SIZE = 500;
	
	@Bean
	public JpaPagingItemReader<Product> productReindexReader() {
		
		return new JpaPagingItemReaderBuilder<Product>()
				.name("productReindexReader")
				.entityManagerFactory(entityManagerFactory)
				.pageSize(CHUNK_SIZE)
				.queryString("SELECT p FROM Product p WHERE p.status = :status")
				.parameterValues(Map.of("status", ProductStatus.ACTIVE))
				.build();
	}
	
	@Bean
	public ItemProcessor<Product, Product> productReindexProcessor() {
		
		return item -> item;
	}
	
	@Bean
	public ItemWriter<Product> productReindexWriter() {
		
		return item -> {
			for (Product product : item) {
				productIndexService.index(product);
			}
		};
	}
	
	@Bean
	public Step productReindexStep(JobRepository jobRepository,
								   PlatformTransactionManager transactionManager) {
		
		return new StepBuilder("productReindexStep", jobRepository)
				.<Product, Product>chunk(CHUNK_SIZE, transactionManager)
				.reader(productReindexReader())
				.processor(productReindexProcessor())
				.writer(productReindexWriter()).build();
	}
	
	@Bean
	public Job productReindexJob(JobRepository jobRepository,
								 Step productReindexStep) {
		
		return new JobBuilder("productReindexJob", jobRepository)
				.incrementer(new RunIdIncrementer())
				.start(productReindexStep).build();
	}
}
