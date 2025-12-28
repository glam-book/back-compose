package com.tlback.scheduling.api;

import java.util.Date;
import java.util.List;

import com.tlback.scheduling.api.dto.JobDTO;
import com.tlback.scheduling.api.dto.JobKeyDTO;

public interface SchedulerManagementService {

    /**
     * Get all scheduled jobs from domain-specific schedulers
     *
     * @return list of all scheduled jobs
     */
    List<JobDTO> getAllJobs();

    /**
     * Get all job keys from domain-specific schedulers
     *
     * @return list of all job keys
     */
    List<JobKeyDTO> getAllJobKeys();

    /**
     * Delete a job by its identity and type
     *
     * @param jobKeyIdentity the job key identity
     * @param jobKeyType     the job key type
     * @return response with status
     */
    boolean deleteJob(String jobKeyIdentity, String jobGroup);

    /**
     * Update the cron expression for an existing job
     *
     * @param jobKeyIdentity the job key identity
     * @param jobKeyType     the job key type
     * @param request        the update request containing the new cron expression
     * @return response with status
     */
    Date updateJobCron(String triggerName, String triggerGroup, String newCronExpression);

    /**
     * Pause a job
     *
     * @param jobKeyIdentity the job key identity
     * @param jobKeyType     the job key type
     * @return response with status
     */
    boolean pauseJob(String jobKeyIdentity, String jobKeyType);

    /**
     * Resume a paused job
     *
     * @param jobKeyIdentity the job key identity
     * @param jobKeyType     the job key type
     * @return response with status
     */
    boolean resumeJob(String jobKeyIdentity, String jobKeyType);

}