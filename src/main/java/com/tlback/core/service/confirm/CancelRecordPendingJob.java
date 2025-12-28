package com.tlback.core.service.confirm;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.tlback.core.service.RecordService;

@Component
public class CancelRecordPendingJob implements Job {
    private RecordService recordService;

    @Override
    public void execute(JobExecutionContext jobCtx) throws JobExecutionException {
        var recPendingId = (Long) jobCtx.getJobDetail().getJobDataMap().get("pendingId");
        var result = recordService.confirmPending(recPendingId, false).block();
        System.out.println("---------- CancelRecordPendingJob: " + recPendingId + " :: result: " + result);
    }
    
}
