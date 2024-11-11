package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.support.DatabaseType;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class BatchJobConfig extends DefaultBatchConfiguration {

    private final DataSource dataSource;
    private final PlatformTransactionManager platformTransactionManager;

    @Override
    public JobRepository jobRepository() {
        JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();

        try {
            jobRepositoryFactoryBean.setDataSource(dataSource);
            jobRepositoryFactoryBean.setTransactionManager(platformTransactionManager);
            jobRepositoryFactoryBean.setDatabaseType(DatabaseType.fromMetaData(dataSource).name());
            jobRepositoryFactoryBean.setIncrementerFactory(new DefaultDataFieldMaxValueIncrementerFactory(dataSource));
            jobRepositoryFactoryBean.setClobType(getClobType());
            jobRepositoryFactoryBean.setTablePrefix(getTablePrefix());
            jobRepositoryFactoryBean.setSerializer(getExecutionContextSerializer());
            jobRepositoryFactoryBean.setConversionService(getConversionService());
            jobRepositoryFactoryBean.setJdbcOperations( new JdbcTemplate(dataSource));
            jobRepositoryFactoryBean.setLobHandler(getLobHandler());
            jobRepositoryFactoryBean.setCharset(getCharset());
            jobRepositoryFactoryBean.setMaxVarCharLength(getMaxVarCharLength());
            jobRepositoryFactoryBean.setIsolationLevelForCreateEnum(Isolation.READ_COMMITTED);
            jobRepositoryFactoryBean.setValidateTransactionState(getValidateTransactionState());
            jobRepositoryFactoryBean.afterPropertiesSet();
            return jobRepositoryFactoryBean.getObject();
        }
        catch (Exception e) {
            throw new BatchConfigurationException("Unable to configure the default job repository", e);
        }
    }

}
