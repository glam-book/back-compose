package com.tlback.scheduling;

import org.quartz.JobDetail;
import org.quartz.Scheduler;

import com.tlback.scheduling.exception.JobCollisionException;

@FunctionalInterface
public interface JobCollisionHandlingStrategy {

    void handle(Scheduler scheduler, JobDetail jobDetail) throws JobCollisionException;
}
