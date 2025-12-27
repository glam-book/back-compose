package com.tlback.core.service.confirm;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tlback.core.model.DomainUserEntity;
import com.tlback.core.model.RecordEntity;
import com.tlback.core.model.contact.Supports;
import com.tlback.core.service.UserService;
import com.tlback.jooq.gen.tables.records.RecordPendingRecord;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@Service
public class RecordPendingSchedulerFacade {
    private final UserService userService;
    private final RecordConfirmScheduler scheduler;
    private final List<ContactConfirmAction> confirmActions;

    @Value("${tlback.pending.confirmation.min-activation:1d}")
    private Duration minActivationTime;
    private Map<Supports, ContactConfirmAction> confirmActionsMap;
    private Map<Supports, Job> contactJobs;

    @PostConstruct
    public void init() {
        this.confirmActionsMap = confirmActions.stream()
                .collect(Collectors.toMap(it -> it.supports(),
                        Function.identity()));

        this.contactJobs = Map.of(Supports.TG, defineTgJob());
    }

    private Job defineTgJob() {
        return jobCtx -> {
            var action = confirmActionsMap.get(Supports.TG);
            if (action == null)
                throw new IllegalStateException("Job pending confirm action shoul not be null");

            var user = jobCtx.getJobDetail().getJobDataMap().get("user");
            var record = jobCtx.getJobDetail().getJobDataMap().get("record");
            var recPendingId = (Long) jobCtx.getJobDetail().getJobDataMap().get("pendingId");

            if ((user != null && user instanceof DomainUserEntity domainUser) &&
                    (record != null && record instanceof RecordEntity recordEntity)) {
                log.info("Sending pending confirm request to user {}", domainUser.getId());
                domainUser.getTgUser()
                        .ifPresentOrElse(tgUser -> action.sendConfirmRequest(tgUser, recPendingId),
                                () -> log.warn("No target contact {} found from domain user", Supports.TG));
            }
        };
    }

    public Mono<Void> scheduleConfirmIfNeeded(RecordPendingRecord pending, RecordEntity targetRecord) {
        var timeLimit = targetRecord.getTsFrom().minus(minActivationTime);
        var currentRequestTime = pending.getRequestTime();

        if (currentRequestTime.isBefore(timeLimit.toLocalDateTime())) {
            var pendingOwner = pending.getClientId();
            return userService.findById(pendingOwner)
                    .doOnSuccess(user -> {
                        var supportedContacts = user.determineSupportedContacts();
                        supportedContacts.forEach(contact -> {
                            var job = contactJobs.get(contact);
                            scheduler.scheduleRecordConfirmation(
                                    targetRecord.getRecordId(),
                                    pendingOwner,
                                    timeLimit,
                                    job.getClass(),
                                    jobData -> {
                                        jobData.put("user", user);
                                        jobData.put("record", targetRecord);
                                    });
                        });
                    }).thenReturn(null);
        }

        return Mono.empty();
    }

}
