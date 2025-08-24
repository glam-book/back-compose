package com.tlback.core.abac.impl;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import com.tlback.core.abac.AbacContext;
import com.tlback.core.abac.AbacService;
import com.tlback.jooq.gen.tables.Record;
import com.tlback.jooq.gen.tables.ServiceInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class JooqAbacServiceImpl implements AbacService {
    private static final Record RECORD_TABLE = Record.RECORD;
    private static final ServiceInfo SERVICE_INFO_TABLE = ServiceInfo.SERVICE_INFO;

    private final DSLContext dsl;

    public Mono<org.jooq.Record1<Long>> fetchRecordOwnerId(Long recordId) {
        return Mono.from(dsl
                .select(RECORD_TABLE.RECORD_OWNER_ID)
                .from(RECORD_TABLE)
                .where(RECORD_TABLE.ID.eq(recordId)));
    }

    @Override
    public Mono<AbacContext> canModifyRecord(Long userId, Long recordId) {
        return fetchRecordOwnerId(recordId)
            .flatMap(r -> {
                Long ownerId = r.value1();
                var decision = ownerId.equals(userId)
                        ? AbacContext.allow()
                        : AbacContext.deny("You do not own this record");
                log.info("Modifying record attempt. Decision: {}, User: {}, Record: {}", decision, userId, recordId);
                return Mono.just(decision);
            })
            .switchIfEmpty(Mono.just(AbacContext.notFound("Record")));
    }

    @Override
    public Mono<AbacContext> canAttachToService(Long userId, Long serviceId) {
        return Mono.from(dsl
                .select(SERVICE_INFO_TABLE.SERVICE_OWNER_ID, SERVICE_INFO_TABLE.EDITABLE)
                .from(SERVICE_INFO_TABLE)
                .where(SERVICE_INFO_TABLE.ID.eq(serviceId)))
                .map(service -> {
                    if (service == null) {
                        return AbacContext.notFound("Service");
                    }

                    Long ownerId = service.get(SERVICE_INFO_TABLE.SERVICE_OWNER_ID);
                    Boolean editable = service.get(SERVICE_INFO_TABLE.EDITABLE);

                    AbacContext decision = null;

                    if (ownerId.equals(userId)) {
                        decision = AbacContext.allow();
                    } else if (Boolean.TRUE.equals(editable)) {
                        decision = AbacContext.allow();
                    } else {
                        decision = AbacContext.deny("You cannot attach records to this service");
                    }

                    log.info("Attaching to service attempt. Decision: {}, User: {}, Service: {}", decision, userId,
                            serviceId);
                    return decision;
                });

    }

    @Override
    public Mono<AbacContext> canUseService(Long userId, Long serviceId) {
        return Mono.from(dsl
                .select(SERVICE_INFO_TABLE.SERVICE_OWNER_ID, SERVICE_INFO_TABLE.EDITABLE)
                .from(SERVICE_INFO_TABLE)
                .where(SERVICE_INFO_TABLE.ID.eq(serviceId)))
                .map(service -> {

                    if (service == null) {
                        return AbacContext.notFound("Service: " + serviceId);
                    }

                    Long ownerId = service.get(SERVICE_INFO_TABLE.SERVICE_OWNER_ID);
                    Boolean editable = service.get(SERVICE_INFO_TABLE.EDITABLE);

                    AbacContext decision;
                    if (ownerId.equals(userId)) {
                        decision = AbacContext.allow();
                    } else if (Boolean.TRUE.equals(editable)) {
                        decision = AbacContext.allow();
                    } else {
                        decision = AbacContext.deny("You do not have access to this service");
                    }

                    log.info("Using service attempt. Decision: {}, User: {}, Service: {}", decision, userId,
                            serviceId);
                    return decision;
                });

    }
}
