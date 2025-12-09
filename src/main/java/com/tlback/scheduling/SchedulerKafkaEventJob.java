package com.tlback.scheduling;

import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.tlback.scheduling.api.SchedulerKafkaEventHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchedulerKafkaEventJob implements Job {
    private SchedulerKafkaEventHandler eventHandler;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (context == null)
            throw new IllegalArgumentException("JobExecutionContext cannot be null");

        if (context.getJobDetail() == null)
            throw new IllegalArgumentException("Job Detail cannot be null");

        if (context.getJobDetail().getJobDataMap() == null)
            throw new IllegalArgumentException("Job Data Map cannot be null");

        var action = (String) context.getJobDetail().getJobDataMap().get("action");
        var topic = (String) context.getJobDetail().getJobDataMap().get("topic");

        Map<String, Object> jobDataMap = context.getJobDetail().getJobDataMap().getWrappedMap();

        log.info("Executing loading job: topic: {} action: {}", topic, action);
        eventHandler.execute(topic, action, jobDataMap);
    }

    @Autowired
    public void setEventHandler(SchedulerKafkaEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

}