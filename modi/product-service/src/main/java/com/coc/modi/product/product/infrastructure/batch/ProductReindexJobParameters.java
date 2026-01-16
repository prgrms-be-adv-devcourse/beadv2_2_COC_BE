package com.coc.modi.product.product.infrastructure.batch;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

public final class ProductReindexJobParameters {
	
	private ProductReindexJobParameters() {
	
	}
	
	public static JobParameters newParameters() {
		
		return new JobParametersBuilder()
				.addLong("time", System.currentTimeMillis())
				.toJobParameters();
	}
}
