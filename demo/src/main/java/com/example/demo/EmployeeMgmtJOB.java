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
//@DisallowConcurrentExecution
//@Transactional(isolation = Isolation.READ_COMMITTED)
public class EmployeeMgmtJOB {

    @Bean(name = "EmployeeMgmtJOB")
    public Job employeeManagementJob(JobRepository jobRepository, Step employeeStep) {
        // Job 객체를 직접 생성
        return new JobBuilder("EmployeeMgmtJOB", jobRepository)
                .start(employeeStep)
                .build();
    }

    @Bean
    public Step employeeStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("employeeStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Executing Employee Management Step");
                    return RepeatStatus.FINISHED;  // 작업 완료 상태 반환
                }, platformTransactionManager).build();
    }

}
