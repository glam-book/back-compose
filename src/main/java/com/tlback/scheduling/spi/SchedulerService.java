package com.tlback.scheduling.spi;

import org.quartz.Job;
import org.quartz.JobKey;

public interface SchedulerService {

    void scheduleJob(String jobKeyIdentity, Class<? extends Job> jobClass);

    void scheduleJob(String jobKeyIdentity, String jobGroup, Class<? extends Job> jobClass);

    JobKey getJobKeyByIdentity(String id);

    public JobKey getJobKeyByIdentity(String id, String group);

    void deleteJobByIdentity(String id);

    void deleteJobByIdentity(String id, String group);
}
