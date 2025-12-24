package com.tlback.core.service.scheduling;

import io.vavr.control.Try;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.tlback.scheduling.QuartzSchedulerService;

import java.time.*;
import java.util.Date;

@Service
@Slf4j
public class RecordConfirmScheduler extends QuartzSchedulerService {

    protected RecordConfirmScheduler(SchedulerFactoryBean schedulerFactoryBean) {
        super(schedulerFactoryBean, "RECORD_CONFIRM", "RECORD_CONFIRM");
    }


    public boolean rescheduleRecordConfirmation(Long recId, Long userId, OffsetDateTime nextStart) {
        removeRecordConfirmation(recId, userId);
        return scheduleRecordConfirmation(recId, userId, nextStart);
    }

    public boolean scheduleRecordConfirmation(Long recId, Long userId, OffsetDateTime startAt) {
        return Try.run(() ->
                        this.scheduleJob(this.getJobKey(recId, userId),
                                this.resourceJobGroup, RecordConfirmJob.class,
                                jobDataMap -> {

                                },
                                (keyMapper, triggerBuilder) -> {
                                    triggerBuilder.startAt(convertToDate(startAt));
                                })
                )
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
