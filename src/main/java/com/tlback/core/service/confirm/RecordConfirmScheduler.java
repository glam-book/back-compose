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
    public static final String NAME_CANCEL = "PENDING_CONFIRMATION_CANCEL";

    public RecordConfirmScheduler(SchedulerFactoryBean schedulerFactoryBean) {
        super(schedulerFactoryBean, NAME + "_JOB", NAME);
    }

    public boolean rescheduleRecordConfirmation(Long recId, Long userId, OffsetDateTime nextStart,
            Class<? extends Job> jobClz, JobDataMapCustomizer jobDataMapCustomizer) {
        removeRecordConfirmation(recId, userId);
        return scheduleRecordPendingConfirmation(recId, userId, nextStart, jobClz, jobDataMapCustomizer);
    }

    public boolean scheduleRecordPendingConfirmation(Long pendingId, Long userId, OffsetDateTime startAt,
            Class<? extends Job> jobClz, JobDataMapCustomizer jobDataMapCustomizer) {
        var startAtConverted = convertToDate(startAt);
        return Try.run(() -> this.scheduleJob(this.getJobKey(pendingId, userId),
                jobClz,
                jobDataMapCustomizer,
                (keyMapper, triggerBuilder) -> triggerBuilder
                    .startAt(startAtConverted)))
                .map(it -> true)
                .onFailure(t -> log.error("Error schedule record confirmation", t))
                .getOrElseGet(t -> false);
    }

    public boolean removeRecordConfirmation(Long recId, Long userId) {
        return Try.run(() -> this.deleteJobByIdentity(getJobKey(recId, userId)))
                .map(it -> true)
                .onFailure(t -> log.error("Error remove record confirmation", t))
                .getOrElseGet(t -> false);
    }

    private String getJobKey(Long pendingId, Long userId) {
        return pendingId + "_" + userId;
    }

    private Date convertToDate(OffsetDateTime date) {
        return Date.from(date.toInstant());
    }

    public boolean scheduleRecordPendingCancelation(Long pendingId, OffsetDateTime cancelTimeLimit) {
        return Try.run(() -> this.scheduleJob(pendingId + "", NAME_CANCEL,
                CancelRecordPendingJob.class,
                jobData -> {
                    jobData.put("pendingId", pendingId);
                },
                (keyMapper, triggerBuilder) -> triggerBuilder.startAt(convertToDate(cancelTimeLimit))))
                .map(it -> true)
                .onFailure(t -> log.error("Error schedule record confirmation", t))
                .getOrElseGet(t -> false);
    }

    public boolean removeRecordPendingCancelation(Long pendingId) {
        return Try.run(() -> this.deleteJobByIdentity(pendingId + "", NAME_CANCEL))
                .map(it -> true)
                .onFailure(t -> log.error("Error remove record confirmation", t))
                .getOrElseGet(t -> false);
    }

}
