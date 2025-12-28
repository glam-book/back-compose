package com.tlback.core.service.confirm;

import java.time.Duration;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tlback.core.dao.jooq.JooqPendingRepository;
import com.tlback.core.model.RecordEntity;
import com.tlback.core.model.utils.PendingConfirmInfo;
import com.tlback.core.service.UserService;
import com.tlback.jooq.gen.tables.records.RecordPendingRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@Service
public class RecordPendingSchedulerFacade {
    private final UserService userService;
    private final RecordConfirmScheduler scheduler;
    private final JooqPendingRepository pendingRepository;

    @Value("${tlback.pending.confirmation.min-activation:1h}")
    private Duration minActivationTime;

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
                                var serviceNames = services.stream().map(it -> it.getServiceName()).collect(Collectors.toSet());
                                supportedContacts.forEach(contact -> {
                                    scheduler.scheduleRecordPendingConfirmation(
                                            targetRecord.getId(),
                                            pendingOwner,
                                            timeLimit,
                                            ConfirmRequestJob.class,
                                            jobData -> {
                                                jobData.put("supports", contact.name());
                                                jobData.put("user", user);
                                                jobData.put("pendingConfirmInfo",
                                                        PendingConfirmInfo.builder()
                                                                .serviceName(serviceNames)
                                                                .from(targetRecord.getTsFrom())
                                                                .to(targetRecord.getTsTo())
                                                                .build());
                                            });
                                    scheduler.scheduleRecordPendingCancelation(targetRecord.getId(), cancelTimeLimit);
                                });
                            }).thenReturn(Boolean.TRUE));
        }

        return Mono.just(Boolean.FALSE);
    }

}
