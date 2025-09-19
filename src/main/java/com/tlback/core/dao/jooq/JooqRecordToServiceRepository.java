package com.tlback.core.dao.jooq;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import com.tlback.jooq.gen.tables.RecordToService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class JooqRecordToServiceRepository {
    public static final RecordToService RECORD_TO_SERVICE_TABLE = RecordToService.RECORD_TO_SERVICE;
    private final DSLContext dsl;

    public Mono<Integer> linkRecordToService(Long recordId, Long serviceId) {
        var sql = dsl.insertInto(com.tlback.jooq.gen.tables.RecordToService.RECORD_TO_SERVICE)
                .set(RECORD_TO_SERVICE_TABLE.RECORD_ID, recordId)
                .set(RECORD_TO_SERVICE_TABLE.SERVICE_ID, serviceId)
                .onConflictDoNothing();
        return Mono.from(sql);
    }

    public Mono<Integer> linkRecordToService(Long recordId, List<Long> serviceIds) {
        var rows = serviceIds.stream()
                .map(serviceId -> DSL.row(recordId, serviceId))
                .toList();

        var sql = dsl.insertInto(RECORD_TO_SERVICE_TABLE)
                .columns(RECORD_TO_SERVICE_TABLE.RECORD_ID, RECORD_TO_SERVICE_TABLE.SERVICE_ID)
                .valuesOfRows(rows)
                .onConflictDoNothing();

        return Mono.from(sql) // здесь вернётся Integer — количество вставленных строк
                .map(count -> count);
    }
}
