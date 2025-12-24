package com.tlback.scheduling;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JobsListenerService implements JobListener {

    @Override
    public String getName() {
        return "JobsListenerService";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        log.info("Job to be executed: {}", context.getJobDetail().getKey().getName());
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        log.info("Job execution vetoed: {}", context.getJobDetail().getKey().getName());
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        log.info("Job was executed: {}" + (jobException != null ? " with exception: " + jobException.getMessage() : ""),
            context.getJobDetail().getKey().getName());
    }
    
}
