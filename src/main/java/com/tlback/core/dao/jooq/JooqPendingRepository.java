package com.tlback.core.dao.jooq;

import java.time.LocalDateTime;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import com.tlback.core.model.RecordPending;
import com.tlback.jooq.gen.tables.ServiceInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class JooqPendingRepository {
    private final com.tlback.jooq.gen.tables.RecordPending pendingTable = com.tlback.jooq.gen.tables.RecordPending.RECORD_PENDING;
    private final com.tlback.jooq.gen.tables.Record recordTable = com.tlback.jooq.gen.tables.Record.RECORD;
    private final ServiceInfo serviceTable = ServiceInfo.SERVICE_INFO;

    private final DSLContext dsl;

    public Mono<RecordPending> createPending(Long initiatorId, Long recordId) {
        var query = dsl.insertInto(pendingTable)
                .set(pendingTable.CONFIRMED, false)
                .set(pendingTable.RECORD_ID, recordId)
                .set(pendingTable.CLIENT_ID, initiatorId)
                .set(pendingTable.REQUEST_TIME, LocalDateTime.now())
                .returning();

        return Mono.from(query)
                .map(rec -> rec.into(RecordPending.class));
    }

    public Mono<RecordPending> createPendingAtomic(Long initiatorId, Long recordId) {
        var query = dsl.insertInto(pendingTable)
                .columns(pendingTable.RECORD_ID, pendingTable.CLIENT_ID, pendingTable.CONFIRMED,
                        pendingTable.REQUEST_TIME)
                .select(
                        DSL.select(DSL.val(recordId), DSL.val(initiatorId), DSL.val(false),
                                DSL.val(LocalDateTime.now()))
                                .where(
                                        DSL.selectCount()
                                                .from(pendingTable)
                                                .where(pendingTable.RECORD_ID.eq(recordId))
                                                .lt(DSL.select(recordTable.RECORD_LIMIT)
                                                        .from(recordTable))))
                .returning();

        log.info(query.toString());
        return Mono.from(query)
                .map(rec -> rec.into(RecordPending.class));
    }
}
