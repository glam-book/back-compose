package com.tlback.scheduling.spi;

import org.quartz.Job;
import org.quartz.JobKey;

public interface SchedulerService {

    void scheduleJob(String jobKeyIdentity, String jobKeyType, Class<? extends Job> jobClass);

    JobKey getJobKeyByIdentity(String id, String type);

    void deleteJobByIdentity(String id, String type);
}
