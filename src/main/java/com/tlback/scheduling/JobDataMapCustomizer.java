package com.tlback.scheduling;

import org.quartz.JobDataMap;

@FunctionalInterface
public interface JobDataMapCustomizer {

    void customize(JobDataMap jobDataMap);

    default JobDataMapCustomizer andThen(JobDataMapCustomizer after) {
        return jobDataMap -> {
            this.customize(jobDataMap);
            after.customize(jobDataMap);
        };
    }
}
