package com.tlback.scheduling;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.tlback.scheduling.exception.JobCollisionException;
import com.tlback.scheduling.exception.SchedulerOperationException;
import com.tlback.scheduling.spi.SchedulerService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Quartz scheduling service implementation for scheduling jobs
 * This class provides the functionality for scheduling and unscheduling jobs
 * using Quartz API. It uses {@link SchedulerFactoryBean} for creating Quartz
 * scheduler instance and provide methods for scheduling jobs with custom
 * trigger and job data map. Automatic reshedule of jobs is also supported.
 * 
 * @author vyacheslav vorobev
 */
@Slf4j
public abstract class QuartzSchedulerService implements SchedulerService {

    /**
     * Scheduler factory for quartz
     */
    @Getter
    protected final SchedulerFactoryBean schedulerFactoryBean;
    protected final String resourceJobGroup;
    protected final String resourceTriggerGroup;

    @Setter
    protected JobCollisionHandlingStrategy collisionHandlingStrategy = (scheduler, jobDetails) -> {
        throw new JobCollisionException("Job is already defined and will not be recreated or rewrited: " + jobDetails.toString());
    };

    @Getter
    protected final Optional<String> triggerDescription;

    @Setter
    protected JobDataMapCustomizer defaultDataMapCustomizer;

    @Setter
    protected TriggerCustomizer<TriggerBuilder<Trigger>> defaTriggerCustomizer;

    protected QuartzSchedulerService(SchedulerFactoryBean schedulerFactoryBean,
            String resourceJobGroup, String resourceTriggerGroup) {
        this(schedulerFactoryBean, null, resourceJobGroup, resourceTriggerGroup);
    }

    protected QuartzSchedulerService(String triggerDescription, SchedulerFactoryBean schedulerFactoryBean,
            String resourceJobGroup, String resourceTriggerGroup) {
        this(schedulerFactoryBean, triggerDescription, resourceJobGroup, resourceTriggerGroup);
    }

    /**
     * Create a new Quartz service
     *
     * @param schedulerFactoryBean scheduler factory for quartz
     * @param resourceJobGroup     job group for the job
     * @param resourceTriggerGroup trigger group for the trigger
     */
    protected QuartzSchedulerService(SchedulerFactoryBean schedulerFactoryBean,
            @Nullable String triggerDescription, String resourceJobGroup,
            String resourceTriggerGroup) {
        this.schedulerFactoryBean = schedulerFactoryBean;
        this.resourceJobGroup = resourceJobGroup;
        this.resourceTriggerGroup = resourceTriggerGroup;
        this.triggerDescription = Optional.ofNullable(triggerDescription);
    }

    /**
     * Delete job by identity
     *
     * @param id   job key identity
     * @param type job key type
     */
    @Override
    public void deleteJobByIdentity(String id, String type) {
        try {
            schedulerFactoryBean.getScheduler()
                    .deleteJob(getJobKeyByIdentity(id, type));
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            throw new SchedulerOperationException(e);
        }
    }

    /**
     * Get job key by identity
     *
     * @param id   job key identity
     * @param type job key type
     * @return job key
     */
    @Override
    public JobKey getJobKeyByIdentity(String id, String type) {
        return new JobKey(QrtzUtils.getJobKeyByIdentity(id, type), resourceJobGroup);
    }

    /**
     * Schedule job by identity
     *
     * @param jobKeyIdentity job key identity
     * @param jobKeyType     job key type
     * @param jobClass       job class
     */
    @Override
    public void scheduleJob(String jobKeyIdentity, String jobKeyType, Class<? extends Job> jobClass) {
        scheduleJob(jobKeyIdentity, jobKeyType, jobClass, null, (i, c) -> c.startNow());
    }

    /**
     * Schedule job by identity
     *
     * @param jobKeyIdentity    job key identity
     * @param jobKeyType        job key type
     * @param jobClass          job class
     * @param triggerCustomizer trigger customizer
     */
    public void scheduleJob(String jobKeyIdentity, String jobKeyType, Class<? extends Job> jobClass,
            @NonNull TriggerCustomizer<TriggerBuilder<Trigger>> triggerCustomizer) {
        scheduleJob(jobKeyIdentity, jobKeyType, jobClass, null, triggerCustomizer);
    }

    /**
     * Schedule job by identity
     *
     * @param jobKeyIdentity       job key identity
     * @param jobKeyType           job key type
     * @param jobClass             job class
     * @param jobDataMapCustomizer job data map customizer
     * @param triggerCustomizer    trigger customizer
     */
    public void scheduleJob(String jobKeyIdentity, String jobKeyType, Class<? extends Job> jobClass,
            @Nullable JobDataMapCustomizer jobDataMapCustomizer,
            @NonNull TriggerCustomizer<TriggerBuilder<Trigger>> triggerCustomizer) {
        var scheduler = schedulerFactoryBean.getScheduler();
        var jobKey = getJobKeyByIdentity(jobKeyIdentity, jobKeyType);

        try {
            var jobDetails = scheduler.getJobDetail(jobKey);
            if (jobDetails != null) {
                collisionHandlingStrategy.handle(scheduler, jobDetails);
            }
        } catch (JobCollisionException e) {
            log.warn(e.getLocalizedMessage());
            return;
        } catch (SchedulerException e) {
            log.warn(e.getMessage(), e);
        }

        var jobData = new JobDataMap();

        if (defaultDataMapCustomizer != null)
            defaultDataMapCustomizer.customize(jobData);

        if (jobDataMapCustomizer != null)
            jobDataMapCustomizer.customize(jobData);

        var jobDetail = newJob(jobClass)
                .ofType(jobClass)
                .withIdentity(jobKey)
                .setJobData(jobData)
                .requestRecovery()
                .build();

        Function<String, TriggerKey> triggerKeyMapper = triggerName -> new TriggerKey(triggerName,
                resourceTriggerGroup);
        var triggerBuilder = newTrigger();

        triggerDescription.ifPresent(triggerBuilder::withDescription);

        if (defaTriggerCustomizer != null)
            defaTriggerCustomizer.customize(triggerKeyMapper, triggerBuilder);

        triggerCustomizer.customize(triggerKeyMapper, triggerBuilder);

        var trigger = triggerBuilder.build();

        try {
            Date nextFireTime = scheduler.scheduleJob(jobDetail, trigger);
            log.info(getLogMsg(nextFireTime, jobKey));
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            throw new SchedulerOperationException(e);
        }
    }

    /**
     * Get log message
     *
     * @param nextFireTime next fire time
     * @param jobKey       job key
     * @return log message
     */
    private String getLogMsg(Date nextFireTime, JobKey jobKey) {
        return String.format("""
                Trigger created: %s
                    Current time: %s
                    Next fire time: %s
                """, jobKey, new Date(), nextFireTime);
    }
}
