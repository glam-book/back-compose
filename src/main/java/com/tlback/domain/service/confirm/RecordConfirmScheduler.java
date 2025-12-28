package com.tlback.domain.service.confirm;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.tlback.domain.dao.jooq.JooqPendingRepository;
import com.tlback.domain.model.RecordEntity;
import com.tlback.domain.model.utils.PendingConfirmInfo;
import com.tlback.domain.service.UserService;
import com.tlback.jooq.gen.tables.records.RecordPendingRecord;
import com.tlback.scheduling.JobDataMapCustomizer;
import com.tlback.scheduling.QuartzSchedulerService;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RecordConfirmScheduler extends QuartzSchedulerService {

    public static final String NAME = "PENDING_CONFIRMATION";
    public static final String NAME_CANCEL = "PENDING_CONFIRMATION_CANCEL";

    private final UserService userService;
    private final JooqPendingRepository pendingRepository;

    @Value("${tlback.pending.confirmation.min-activation:1h}")
    private Duration minActivationTime;

    public RecordConfirmScheduler(SchedulerFactoryBean schedulerFactoryBean,
            UserService userService, JooqPendingRepository pendingRepository) {
        super(schedulerFactoryBean, NAME + "_JOB", NAME);
        this.userService = userService;
        this.pendingRepository = pendingRepository;
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

    public Mono<Boolean> scheduleConfirmIfNeeded(
            RecordPendingRecord pending,
            RecordEntity targetRecord) {

        var timeLimit = targetRecord.getTsFrom()
                .minus(minActivationTime)
                .minusMinutes(2);

        var cancelTimeLimit = timeLimit.plusHours(2);
        var currentRequestTime = pending.getRequestTime();
        var isBefore = currentRequestTime.isBefore(timeLimit.toLocalDateTime());

        if (isBefore) {
            var pendingOwner = pending.getClientId();
            return userService.findById(pendingOwner)
                    .flatMap(user -> pendingRepository.findServicesByPendingId(pending.getId())
                            .collectList()
                            .doOnSuccess(services -> {
                                var supportedContacts = user.determineSupportedContacts();
                                var serviceNames = services.stream().map(it -> it.getServiceName())
                                        .collect(Collectors.toSet());
                                supportedContacts.forEach(contact -> {
                                    scheduleRecordPendingConfirmation(
                                            targetRecord.getId(),
                                            pendingOwner,
                                            timeLimit,
                                            ConfirmRequestJob.class,
                                            jobData -> {
                                                jobData.put("supports", contact.name());
                                                jobData.put("user", user);
                                                jobData.put("pendingConfirmInfo",
                                                        PendingConfirmInfo.builder()
                                                                .pendingId(pending.getId())
                                                                .serviceName(serviceNames)
                                                                .from(targetRecord.getTsFrom())
                                                                .to(targetRecord.getTsTo())
                                                                .build());
                                            });
                                    scheduleRecordPendingCancelation(targetRecord.getId(), cancelTimeLimit);
                                });
                            }).thenReturn(Boolean.TRUE));
        }

        return Mono.just(Boolean.FALSE);
    }

}
