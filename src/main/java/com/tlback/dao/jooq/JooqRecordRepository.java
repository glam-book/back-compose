package com.tlback.dao.jooq;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.SelectOnConditionStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import com.tlback.common.daofilter.RecordFilter;
import com.tlback.tools.RxUtils;
import com.tlback.domain.RecordEntity;
import com.tlback.jooq.gen.tables.DomainUser;
import com.tlback.jooq.gen.tables.Record;
import com.tlback.jooq.gen.tables.RecordPending;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class JooqRecordRepository {
    private static final Record recordTable = Record.RECORD;
    private static final RecordPending recordPendingTable = RecordPending.RECORD_PENDING;
    private static final DomainUser userTable = DomainUser.DOMAIN_USER;

    private final DSLContext dsl;

    public Flux<RecordEntity> findByFilter(RecordFilter filter) {
        var query = fetch(dsl).where(DSL.noCondition());

        if (filter.serviceInfoId() != null)
            query = query.and(recordTable.SERVICE_INFO_ID.eq(filter.serviceInfoId()));

        if (filter.recordOwnerId() != null)
            query = query.and(recordTable.RECORD_OWNER_ID.eq(filter.recordOwnerId()));

        if (filter.clientId() != null)
            query = query.and(recordPendingTable.CLIENT_ID.eq(filter.clientId()));

        if (filter.dateFrom() != null)
            query = query.and(recordTable.TIME_FROM.greaterThan(filter.dateFrom()));

        if (filter.dateTo() != null)
            query = query.and(recordTable.TIME_TO.lessThan(filter.dateTo()));

        if (filter.isPublic() != null)
            query = query.and(recordTable.IS_PUBLIC.eq(filter.isPublic()));

        return RxUtils.fluxIterable(query, it -> {
            Map<Long, RecordEntity> records = new HashMap<>();
            it.forEach(rec -> {
                var record = records.computeIfAbsent(rec.get(recordTable.ID), k -> mapJustRecEntity(rec));
                var pending = rec.into(recordPendingTable.fields()).into(com.tlback.domain.RecordPending.class);
                if (pending.getPendingOwner() == null)
                    pending.setPendingOwner(
                            rec.into(userTable.fields()).into(com.tlback.domain.DomainUserEntity.class));
                record.getRecordPendings().add(pending);
            });
            return records.values();
        });
    }

    public static RecordEntity mapJustRecEntity(org.jooq.Record rec) {
        return rec.into(recordTable.fields()).map(mapper -> {
            var offsetTz = mapper.get(recordTable.TZ);
            var tz = ZoneOffset.of(offsetTz);

            var timeFrom = mapper.get(recordTable.TIME_FROM);
            var timeTo = mapper.get(recordTable.TIME_TO);

            var entity = new RecordEntity();
            entity.setId(rec.get(recordTable.ID));
            entity.setServiceInfoId(rec.get(recordTable.SERVICE_INFO_ID));
            entity.setRecordOwnerId(rec.get(recordTable.RECORD_OWNER_ID));
            entity.setIsPublic(rec.get(recordTable.IS_PUBLIC));
            entity.setTimeFrom(timeFrom.atOffset(tz));
            entity.setTimeTo(timeTo.atOffset(tz));
            entity.setTz(tz);
            return entity;
        });
    }

    public static SelectOnConditionStep<org.jooq.Record> fetch(DSLContext dsl) {
        return dsl.select().from(recordTable).join(recordPendingTable)
                .on(recordTable.ID.eq(recordPendingTable.RECORD_ID)).join(userTable)
                .on(recordPendingTable.CLIENT_ID.eq(userTable.ID));
    }
}
