package com.tlback.core.service.confirm;

import java.time.OffsetDateTime;
import java.util.Date;

import org.quartz.Job;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.tlback.scheduling.JobDataMapCustomizer;
import com.tlback.scheduling.QuartzSchedulerService;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RecordConfirmScheduler extends QuartzSchedulerService {

    public static final String NAME = "PENDING_CONFIRMATION";

    public RecordConfirmScheduler(SchedulerFactoryBean schedulerFactoryBean) {
        super(schedulerFactoryBean, NAME, NAME);
    }

    public boolean rescheduleRecordConfirmation(Long recId, Long userId, OffsetDateTime nextStart,
            Class<? extends Job> jobClz, JobDataMapCustomizer jobDataMapCustomizer) {
        removeRecordConfirmation(recId, userId);
        return scheduleRecordConfirmation(recId, userId, nextStart, jobClz, jobDataMapCustomizer);
    }

    public boolean scheduleRecordConfirmation(Long recId, Long userId, OffsetDateTime startAt,
            Class<? extends Job> jobClz, JobDataMapCustomizer jobDataMapCustomizer) {
        return Try.run(() -> this.scheduleJob(this.getJobKey(recId, userId),
                this.resourceJobGroup, jobClz,
                jobDataMapCustomizer,
                (keyMapper, triggerBuilder) -> triggerBuilder.startAt(convertToDate(startAt))))
                .map(it -> true)
                .onFailure(t -> log.error("Error schedule record confirmation", t))
                .getOrElseGet(t -> false);
    }

    public boolean removeRecordConfirmation(Long recId, Long userId) {
        return Try.run(() -> this.deleteJobByIdentity(getJobKey(recId, userId), resourceJobGroup))
                .map(it -> true)
                .onFailure(t -> log.error("Error remove record confirmation", t))
                .getOrElseGet(t -> false);
    }

    private String getJobKey(Long recId, Long userId) {
        return recId + "_" + userId;
    }

    private Date convertToDate(OffsetDateTime date) {
        var instant = date.toInstant();
        return Date.from(instant);
    }

}
