package com.tlback.core.config;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import javax.sql.DataSource;

import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.PlatformTransactionManager;

import com.tlback.scheduling.JobsListenerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class QuartzConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String jdbcUser;

    @Value("${spring.datasource.password}")
    private String jdbcPassword;

    @Bean
    DataSource dataSource() {
        var dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.postgresql.Driver");
        dataSourceBuilder.url(jdbcUrl);
        dataSourceBuilder.username(jdbcUser);
        dataSourceBuilder.password(jdbcPassword);
        return dataSourceBuilder.build();
    }

    @Bean
    @Primary
    PlatformTransactionManager txManager(DataSource source) {
        return new DataSourceTransactionManager(source);
    }

    /**
     * Main quartz bean configuration
     */
    @Bean
    SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource,
                                              PlatformTransactionManager txManager,
                                              SpringBeanJobFactory jobFactory,
                                              JobsListenerService jobsListenerService, Trigger... triggers) throws IOException {

        var schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setQuartzProperties(quartzProps());
        schedulerFactory.setDataSource(dataSource);
        schedulerFactory.setTransactionManager(txManager);
        schedulerFactory.setJobFactory(jobFactory);
        schedulerFactory.setGlobalJobListeners(jobsListenerService);
        schedulerFactory.setStartupDelay(5);
        schedulerFactory.setOverwriteExistingJobs(true);

        if (triggers != null && triggers.length > 0) {
            schedulerFactory.setTriggers(triggers);
            log.info("Quartz triggers: {}", triggers.length);
        }

        return schedulerFactory;
    }

    /**
     * Job factory with current application context to inject beans into job
     * instances
     */
    @Bean
    SpringBeanJobFactory springBeanJobFactory(ApplicationContext ctx) {
        var jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(ctx);
        return jobFactory;
    }

    /**
     * Quartz properties
     */
    private Properties quartzProps() throws IOException {
        var factoryBean = new PropertiesFactoryBean();
        factoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    @Bean
    CommandLineRunner printQuartzConfig(SchedulerFactoryBean schedulerFactoryBean) {
        return args -> {
            var scheduler = schedulerFactoryBean.getScheduler();
            log.info(MessageFormat.format("""
                            
                            --- Quartz Scheduler Info ---
                            Scheduler Name: {0}
                            Instance ID: {1}
                            Scheduler Class: {2}
                            Is Started: {3}
                            Is In Standby Mode: {4}
                            Is Shutdown: {5}
                            Job Store Class: {6}
                            Thread Pool Class: {7}
                            Number of Jobs Executed: {8}
                            Clustered: {9}
                            Version: {10}
                            --------------------------------
                            """,
                    scheduler.getSchedulerName(),
                    scheduler.getSchedulerInstanceId(),
                    scheduler.getClass().getName(),
                    scheduler.isStarted(),
                    scheduler.isInStandbyMode(),
                    scheduler.isShutdown(),
                    scheduler.getMetaData().getJobStoreClass().getName(),
                    scheduler.getMetaData().getThreadPoolClass().getName(),
                    scheduler.getMetaData().getNumberOfJobsExecuted(),
                    scheduler.getMetaData().isJobStoreClustered(),
                    scheduler.getMetaData().getVersion()));
        };
    }

}
