package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@RestController
@RequestMapping("/batch")
public class BatchController  {


    @Autowired
    private BatchSchedulerService batchSchedulerService;

    @Autowired
    private Scheduler scheduler;

//     배치 목록을 수정하는 API
    @PostMapping("/update")
    public String updateBatchJobList(@RequestBody BatchJobRequest batchJobRequest) {
        try {
            // 배치 작업 목록을 수정하고
            batchSchedulerService.updateBatchJobList(batchJobRequest);

            // 변경 사항에 따라 Quartz 스케줄러 재설정
            batchSchedulerService.reconfigureScheduler(batchJobRequest);

            return "Batch Job List Updated and Scheduler Reconfigured";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
