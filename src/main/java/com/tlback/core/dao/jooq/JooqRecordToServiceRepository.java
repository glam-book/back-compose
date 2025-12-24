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
        public static final RecordToService recordToServiceTable = RecordToService.RECORD_TO_SERVICE;
        private final DSLContext dsl;

        public Mono<Integer> linkRecordToService(Long recordOwnerId, Long recordId, Long serviceId) {
                var sql = dsl.insertInto(com.tlback.jooq.gen.tables.RecordToService.RECORD_TO_SERVICE)
                                .set(recordToServiceTable.RECORD_OWNER_ID, recordOwnerId)
                                .set(recordToServiceTable.RECORD_ID, recordId)
                                .set(recordToServiceTable.SERVICE_ID, serviceId)
                                .onConflictDoNothing();
                return Mono.from(sql);
        }

        public Mono<Integer> linkRecordToService(Long recordOwnerId, Long recordId, List<Long> serviceIds) {
                var delete = dsl.deleteFrom(recordToServiceTable)
                                .where(recordToServiceTable.RECORD_OWNER_ID.eq(recordOwnerId))
                                .and(recordToServiceTable.RECORD_ID.eq(recordId))
                                .and(recordToServiceTable.SERVICE_ID.notIn(serviceIds));

                var insert = dsl.insertInto(recordToServiceTable)
                                .columns(recordToServiceTable.RECORD_OWNER_ID, recordToServiceTable.RECORD_ID, recordToServiceTable.SERVICE_ID)
                                .valuesOfRows(serviceIds.stream()
                                                .map(serviceId -> DSL.row(recordOwnerId, recordId, serviceId))
                                                .toList())
                                .onConflictDoNothing();

                return Mono.from(delete)
                                .then(Mono.from(insert));
        }
}
