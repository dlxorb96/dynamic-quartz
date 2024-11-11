package com.example.demo;

import jakarta.annotation.PostConstruct;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class SchedulerConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;  // 데이터베이스 조회에 사용
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private JobLauncher jobLauncher;  // Spring Batch JobLauncher


    @PostConstruct
    public Scheduler scheduler() throws SchedulerException {
        // Quartz 스케줄러 설정
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();

        // SpringBeanJobFactory를 설정하여 Quartz가 Spring 의존성 주입을 사용하도록 설정
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext); // Spring 컨텍스트를 주입
        scheduler.setJobFactory(jobFactory);

        scheduler.start();
        return scheduler;
    }

    @Bean
    public void scheduleJobs() throws SchedulerException {

        Scheduler scheduler = scheduler();
        // 배치 관리 테이블에서 모든 행을 가져옴
        List<Map<String, Object>> jobs = jdbcTemplate.queryForList("SELECT * FROM batch_management");

        for (Map<String, Object> job : jobs) {
            String jobName = (String) job.get("job_name");
            String batchFrequency = (String) job.get("batch_frequency");
            String frequencyUnit = (String) job.get("frequency_unit");
            String batchTime = job.get("batch_time").toString();  // HH:MM:SS 형식
            String daysOfWeek = (String) job.get("days_of_week");

            // Cron 표현식을 생성하는 로직
            String cronExpression = createCronExpression(batchFrequency, frequencyUnit, batchTime, daysOfWeek);

            String jobKey = UUID.randomUUID().toString();  // 고유한 job_key 생성

            // JobDetail과 Trigger 생성
            JobDetail jobDetail = JobBuilder.newJob(QuartzBatchJob.class)
                    .withIdentity(jobName, jobKey)
                    .usingJobData("jobName", jobName)  // Job 이름을 JobData로 설정
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "Trigger", "batch-triggers")
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            // Quartz 스케줄러에 Job과 Trigger를 등록
            scheduler.scheduleJob(jobDetail, trigger);
        }
    }

    // Cron 표현식 생성 메서드
    public String createCronExpression(String batchFrequency, String frequencyUnit, String batchTime, String daysOfWeek) {
        // 예제: "0 0/10 * * * ?" (10분 간격)
        String[] timeParts = batchTime.split(":");
//        String seconds = "0";  // 기본적으로 0초에 실행
        String minutes = timeParts[1];
        String hours = timeParts[0];

        String cronExpression = "";

        switch (frequencyUnit) {
            case "초":
                // 초 주기로 설정 (예: "0/5 * * * * ?"는 5초마다 실행)
                cronExpression = String.format("0/%s * * * * ?", batchFrequency);
                break;
            case "분":
                cronExpression = String.format("0 0/%s * * * ?", batchFrequency);
                break;
            case "시간":
                cronExpression = String.format("0 %s 0/%s * * ?", minutes, batchFrequency);
                break;
            case "일":
                cronExpression = String.format("0 %s %s * * ?", minutes, hours);
                break;
            case "주":
                cronExpression = String.format("0 %s %s ? * %s", minutes, hours, daysOfWeek);
                break;
            case "월":
                cronExpression = String.format("0 %s %s 1 * ?", minutes, hours);
                break;
        }

        return cronExpression;
    }


}

