package com.tlback.scheduling;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import com.tlback.scheduling.api.SchedulerManagementService;
import com.tlback.scheduling.api.dto.JobDTO;
import com.tlback.scheduling.api.dto.JobKeyDTO;
import com.tlback.scheduling.api.dto.TriggerDTO;
import com.tlback.scheduling.exception.InvalidCronExpressionException;
import com.tlback.scheduling.exception.JobNotFoundException;
import com.tlback.scheduling.exception.SchedulerOperationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing Quartz scheduler jobs and triggers.
 * Provides methods to retrieve, pause, resume, and update job triggers.
 * Handles exceptions and logs errors.
 * Uses SchedulerFactoryBean for scheduler operations.
 * Uses JobDTO, TriggerDTO, and JobKeyDTO for data transfer.
 * 
 * @author vyacheslav vorobev
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerManagementServiceImpl implements SchedulerManagementService {

    private final SchedulerFactoryBean schedulerFactoryBean;

    /**
     * Get all scheduled jobs from domain-specific schedulers
     *
     * @return list of all scheduled jobs
     */
    @Override
    public List<JobDTO> getAllJobs() {
        try {
            var jobs = schedulerFactoryBean.getScheduler()
                    .getJobKeys(GroupMatcher.anyGroup()).stream().toList();

            return jobs.stream()
                    .map(this::toJobDto)
                    .toList();

        } catch (Exception e) {
            log.error("Error retrieving jobs", e);
            throw new SchedulerOperationException("Failed to retrieve jobs: " + e.getMessage(), e);
        }
    }

    private JobDTO toJobDto(JobKey jobKey) {
        List<TriggerDTO> triggers = new ArrayList<>();
        try {
            triggers = getTriggers(jobKey);
        } catch (SchedulerException e) {
            log.error("Error retrieving triggers for job", e);
        }
        return JobDTO.builder()
                .jobClass(jobKey.getClass().getName())
                .jobName(jobKey.getName())
                .jobGroup(jobKey.getGroup())
                .triggers(triggers)
                .build();
    }

    private List<TriggerDTO> getTriggers(JobKey jobKey) throws SchedulerException {
        return schedulerFactoryBean.getScheduler().getTriggersOfJob(jobKey)
                .stream()
                .map(trigger -> TriggerDTO.builder()
                        .triggerName(trigger.getKey().getName())
                        .triggerGroup(trigger.getKey().getGroup())
                        .previousFireTime(trigger.getPreviousFireTime())
                        .nextFireTime(trigger.getNextFireTime())
                        .build())
                .toList();
    }

    /**
     * Get all job keys from domain-specific schedulers
     *
     * @return list of all job keys
     */
    @Override
    public List<JobKeyDTO> getAllJobKeys() {
        try {
            var scheduler = schedulerFactoryBean.getScheduler();
            return getJobKeysFromScheduler(scheduler);
        } catch (Exception e) {
            log.error("Error retrieving job keys", e);
            throw new SchedulerOperationException("Failed to retrieve job keys: " + e.getMessage(), e);
        }
    }

    private List<JobKeyDTO> getJobKeysFromScheduler(Scheduler scheduler) throws SchedulerException {
        return scheduler.getJobKeys(GroupMatcher.anyGroup())
                .stream()
                .map(jobKey -> JobKeyDTO.builder()
                        .identity(jobKey.getName())
                        .group(jobKey.getGroup())
                        .fullKey(jobKey.toString())
                        .build())
                .toList();
    }

    /**
     * Delete a job by its identity and type
     *
     * @param jobKeyIdentity the job key identity
     * @param jobKeyType     the job key type
     * @return response with status
     */
    @Override
    public boolean deleteJob(String jobKeyIdentity, String jobGroup) {
        try {
            var scheduler = schedulerFactoryBean.getScheduler();
            var jobKey = new JobKey(jobKeyIdentity, jobGroup);
            return scheduler.deleteJob(jobKey);
        } catch (Exception e) {
            log.error("Error deleting job", e);
            throw new SchedulerOperationException("Failed to delete job: " + e.getMessage(), e);
        }
    }

    /**
     * Update the cron expression for an existing job
     *
     * @param jobKeyIdentity the job key identity
     * @param jobKeyType     the job key type
     * @param request        the update request containing the new cron expression
     * @return response with status
     */
    @Override
    public Date updateJobCron(String triggerName, String triggerGroup, String newCronExpression) {
        try {
            // Validate cron expression
            validateCron(newCronExpression);
            var triggerKey = new TriggerKey(triggerName, triggerGroup);
            var newTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(newCronExpression));

            return schedulerFactoryBean.getScheduler().rescheduleJob(triggerKey, newTrigger.build());
        } catch (Exception e) {
            log.error("Error updating job cron expression", e);
            throw new SchedulerOperationException("Failed to update job cron: " + e.getMessage(), e);
        }
    }

    private void validateCron(String cron) throws InvalidCronExpressionException {
        if (!CronExpression.isValidExpression(cron))
            throw new InvalidCronExpressionException("Invalid cron expression: " + cron);
    }

    /**
     * Pause a job
     *
     * @param jobKeyIdentity the job key identity
     * @param jobKeyType     the job key type
     * @return response with status
     */
    @Override
    public boolean pauseJob(String jobKeyIdentity, String jobKeyType) {
        try {
            var scheduler = schedulerFactoryBean.getScheduler();
            var jobKey = new JobKey(jobKeyIdentity, jobKeyType);
            var jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail != null) {
                scheduler.pauseJob(jobKey);
                return true;
            } else {
                throw new JobNotFoundException(
                        "Job not found with identity: " + jobKeyIdentity + " and type: " + jobKeyType);
            }
        } catch (JobNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error pausing job", e);
            throw new SchedulerOperationException("Failed to pause job: " + e.getMessage(), e);
        }
    }

    /**
     * Resume a paused job
     *
     * @param jobKeyIdentity the job key identity
     * @param jobKeyType     the job key type
     * @return response with status
     */
    @Override
    public boolean resumeJob(String jobKeyIdentity, String jobKeyType) {
        try {
            var scheduler = schedulerFactoryBean.getScheduler();
            var jobKey = new JobKey(jobKeyIdentity, jobKeyType);
            var jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail != null) {
                scheduler.resumeJob(jobKey);
                return true;
            } else {
                throw new JobNotFoundException(
                        "Job not found with identity: " + jobKeyIdentity + " and type: " + jobKeyType);
            }
        } catch (JobNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error resuming job", e);
            throw new SchedulerOperationException("Failed to resume job: " + e.getMessage(), e);
        }
    }

}