package com.coc.modi.seller.settlement.batch.support;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@TestConfiguration
public class SettlementPayoutTestConfig {

    @Bean
    public Job settlementPayoutTestJob(JobRepository jobRepository, Step settlementPayoutStep) {


        return new JobBuilder("settlementPayoutTestJob", jobRepository)
                .listener(settlementBatchJobListener)
                .start(settlementPayoutStep)
                .build();
    }

    @Bean
    public DataSourceInitializer batchSchemaInitializer(DataSource dataSource) {

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-h2.sql"));

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
