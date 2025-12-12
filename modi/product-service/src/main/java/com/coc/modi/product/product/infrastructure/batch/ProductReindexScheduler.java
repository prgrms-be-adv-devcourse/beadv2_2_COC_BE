package com.coc.modi.product.product.infrastructure.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductReindexScheduler {

    private final JobLauncher jobLauncher;
    private final Job reindexJob;

    @Scheduled(cron = "0 0 3 * * *")
    public void reindex() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            log.info("[Batch] productReindexJob start");
            jobLauncher.run(reindexJob, params);
            log.info("[Batch] productReindexJob end");
        } catch (Exception e) {
            log.error("[Batch] productReindexJob error", e);
        }
    }
}
