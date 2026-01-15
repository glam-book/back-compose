package com.tlback.domain.service.confirm;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.tlback.domain.dao.jooq.JooqPendingRepository;
import com.tlback.domain.model.RecordEntity;
import com.tlback.domain.model.RecordPending;
import com.tlback.domain.model.utils.PendingConfirmInfo;
import com.tlback.domain.service.UserService;
import com.tlback.scheduling.JobDataMapCustomizer;
import com.tlback.scheduling.QuartzSchedulerService;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class RecordConfirmScheduler extends QuartzSchedulerService {

    public static final String NAME = "PENDING_CONFIRMATION";
    public static final String NAME_CANCEL = "PENDING_CONFIRMATION_CANCEL";

    private final UserService userService;
    private final JooqPendingRepository pendingRepository;

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

    public Mono<Void> scheduleConfirmation(
            RecordPending pending,
            RecordEntity targetRecord,
            OffsetDateTime startAt) {

        var pendingOwner = pending.getClientId();
        return userService.findById(pendingOwner)
                .zipWhen(user -> pendingRepository.findServicesByPendingId(pending.getId())
                        .collectList())
                .flatMapMany(tuple -> {
                    var user = tuple.getT1();
                    var services = tuple.getT2();

                    var supportedContacts = user.determineSupportedContacts();
                    var serviceNames = services.stream()
                            .map(s -> s.getServiceName())
                            .collect(Collectors.toSet());

                    return Flux.fromIterable(supportedContacts)
                            .flatMap(contact -> Mono.fromRunnable(() -> scheduleRecordPendingConfirmation(
                                    targetRecord.getId(),
                                    pendingOwner,
                                    startAt,
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
                                    }))
                                    .subscribeOn(Schedulers.boundedElastic()));
                })
                .then();

    }

}
