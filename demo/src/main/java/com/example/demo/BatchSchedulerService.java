package com.example.demo;

import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BatchSchedulerService {
    @Autowired
    private Scheduler scheduler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SchedulerConfig schedulerConfig;


    // 배치 작업 목록을 업데이트하는 메소드
    public void updateBatchJobList(BatchJobRequest batchJobRequest) {
        String sql = "UPDATE batch_management SET " +
                "batch_frequency = ?, " +
                "frequency_unit = ? " +
                "WHERE batch_id = ?";

        jdbcTemplate.update(sql,
                batchJobRequest.getBatchFrequency(),
                batchJobRequest.getFrequencyUnit(),
                batchJobRequest.getBatchId());
    }

    public List<BatchJobRequest> selectBatchJobList() {
        // 전체 배치 목록을 조회하는 SQL 쿼리
        String sql = "SELECT batch_id, batch_name, job_name, batch_frequency, " +
                "frequency_unit, batch_time, days_of_week, days_of_month, is_adw " +
                "FROM batch_management";

        // 쿼리 실행 및 결과 반환
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(BatchJobRequest.class));

    }

    // Quartz 스케줄러를 새롭게 설정하는 메소드
    public void reconfigureScheduler(BatchJobRequest batchJobRequest) throws SchedulerException {
        // 기존 작업이 있는 경우 제거
//        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyGroup())) {
//            scheduler.deleteJob(jobKey);
//        }

        scheduler.clear();

        List<BatchJobRequest> batchJobList = this.selectBatchJobList();
//        for (BatchJobRequest batchJob : batchJobList) {
//            String jobName = batchJob.getJobName();  // BatchJobRequest에서 jobName 가져오기
//
//            // jobName이 비어있지 않으면 기존 작업 삭제
//            if (jobName != null && !jobName.isEmpty()) {
//                // 고유한 jobKey를 생성하여 삭제
////                String uniqueJobKey = jobName + "-" + UUID.randomUUID().toString();  // jobName과 UUID로 고유한 jobKey 생성
//                JobKey jobKey = new JobKey(jobName, "batchGroup");  // jobKey는 유니크하게 설정
//
//                // 기존 작업이 존재하면 삭제
//                if (scheduler.checkExists(jobKey)) {
//                    scheduler.deleteJob(jobKey);  // 기존 작업 삭제
//                }
//            }
//        }


        // 새롭게 배치 작업을 등록

        for (BatchJobRequest batchJob : batchJobList) {
            String jobName = batchJob.getJobName();  // BatchJobRequest에서 jobName 가져오기
            String batchFrequency = batchJob.getBatchFrequency();
            String frequencyUnit = batchJob.getFrequencyUnit();
            String batchTime = batchJob.getBatchTime();
            String daysOfWeek = batchJob.getDaysOfWeek();


            if (jobName == null || jobName.isEmpty()) {
                throw new IllegalArgumentException("Job name cannot be null or empty");
            }

            String cronExpression = schedulerConfig.createCronExpression(batchFrequency, frequencyUnit, batchTime, daysOfWeek);
            String jobKey = UUID.randomUUID().toString();  // 고유한 job_key 생성

            // JobDetail 생성 (Quartz 작업 정의)
            JobDetail jobDetail = JobBuilder.newJob(QuartzBatchJob.class)  // QuartzBatchJob 클래스는 실제 실행할 작업을 정의
                    .withIdentity(jobName, jobKey)  // 작업 이름과 그룹을 설정
                    .usingJobData("jobName", jobName)  // Job 이름을 JobData로 설정
                    .build();

            // Trigger 생성 (작업을 언제 실행할지 정의)
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "Trigger", "batchGroup")  // 트리거 이름과 그룹을 설정
                    .startNow()  // 즉시 시작
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            // Quartz 스케줄러에 JobDetail과 Trigger 등록
            scheduler.scheduleJob(jobDetail, trigger);
        }
    }


}
