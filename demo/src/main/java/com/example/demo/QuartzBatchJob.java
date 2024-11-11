package com.example.demo;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
@DisallowConcurrentExecution
public class QuartzBatchJob implements Job {

    @Autowired
    private ApplicationContext applicationContext;  // Spring ApplicationContext
    @Autowired
    private JobLauncher jobLauncher;  // Spring Batch JobLauncher


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getJobDataMap().getString("jobName");

        try {
            // ApplicationContext에서 Job Bean을 가져옴
            org.springframework.batch.core.Job job = (org.springframework.batch.core.Job) applicationContext.getBean(jobName);


            // JobParameters 생성
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            // Job 실행
            jobLauncher.run(job, jobParameters);
        } catch (Exception e) {
            throw new JobExecutionException("Failed to execute job: " + jobName, e);
        }

    }
}
