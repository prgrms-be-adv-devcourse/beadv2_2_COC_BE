package com.coc.modi.product.product.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.product.product.exception.ProductInternalException;
import com.coc.modi.product.product.presentation.dto.ProductReindexResponse;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductReindexController {
	
	private final JobLauncher jobLauncher;
	private final Job productReindexJob;
	
	public ProductReindexController(
			JobLauncher jobLauncher,
			@Qualifier("productReindexJob") Job productReindexJob
	) {
		this.jobLauncher = jobLauncher;
		this.productReindexJob = productReindexJob;
	}
	
	@PostMapping("/reindex")
	public ApiResponse<ProductReindexResponse> reindex() {
		
		try {
			JobParameters params = new JobParametersBuilder()
					.addLong("time", System.currentTimeMillis())
					.toJobParameters();
			JobExecution execution = jobLauncher.run(productReindexJob, params);
			return ApiResponse.ok(new ProductReindexResponse(execution.getId(), execution.getStatus().toString()));
		} catch (Exception e) {
			throw new ProductInternalException("상품 리인덱싱 배치 실행에 실패했습니다.", e);
		}
	}
}
