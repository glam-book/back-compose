package com.tlback.core.dao.jooq;

import java.time.LocalDateTime;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import com.tlback.core.model.RecordPending;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class JooqPendingRepository {
    private final com.tlback.jooq.gen.tables.RecordPending pendingTable = com.tlback.jooq.gen.tables.RecordPending.RECORD_PENDING;
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
}
