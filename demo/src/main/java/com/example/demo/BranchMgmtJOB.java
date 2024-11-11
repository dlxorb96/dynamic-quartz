package com.example.demo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BranchMgmtJOB {


    @Bean(name = "BranchMgmtJOB")
    public Job branchManagementJob(JobRepository jobRepository, Step branchStep) {
        return new JobBuilder("BranchMgmtJOB", jobRepository)
                .start(branchStep)  // 'branchStep'을 첫 번째 Step으로 설정
                .build();  // Job 빌드
    }

    @Bean
    public Step branchStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("branchStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Executing Branch Management Step");
                    return RepeatStatus.FINISHED;  // 작업 완료 상태 반환
                }, platformTransactionManager)
                .build();  // Step 빌드
    }

}
