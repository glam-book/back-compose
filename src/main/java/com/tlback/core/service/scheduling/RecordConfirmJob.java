package com.tlback.core.service.scheduling;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class RecordConfirmJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("ABOBOBBOOBOBAAAAAAAA _____ " + (System.currentTimeMillis() / 1000));
    }
}
