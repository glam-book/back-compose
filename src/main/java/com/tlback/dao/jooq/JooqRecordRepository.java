package com.tlback.dao.jooq;

import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import com.tlback.common.daofilter.RecordFilter;
import com.tlback.dao.jooq.modules.JoinModule;
import com.tlback.domain.RecordEntity;
import com.tlback.domain.ServiceInfoEntity;
import com.tlback.jooq.gen.tables.DomainUser;
import com.tlback.jooq.gen.tables.Record;
import com.tlback.jooq.gen.tables.RecordPending;
import com.tlback.jooq.gen.tables.ServiceInfo;
import com.tlback.tools.RxUtils;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class JooqRecordRepository {
    public static final Record recordTable = Record.RECORD;
    public static final RecordPending recordPendingTable = RecordPending.RECORD_PENDING;
    public static final ServiceInfo serviceInfoTable = ServiceInfo.SERVICE_INFO;
    public static final DomainUser userTable = DomainUser.DOMAIN_USER;

    public static final JoinModule JOIN_SERVICE_INFO = mainFetch -> mainFetch.join(serviceInfoTable)
            .on(recordTable.SERVICE_INFO_ID.eq(serviceInfoTable.ID));

    public static final JoinModule JOIN_RECORD_PENDINGS = mainFetch -> mainFetch.join(recordPendingTable)
            .on(recordTable.ID.eq(recordPendingTable.RECORD_ID));

    public static final JoinModule JOIN_RECORD_PENDING_USER_INFO = mainFetch -> mainFetch.join(userTable)
            .on(recordTable.RECORD_OWNER_ID.eq(userTable.ID));

    public static final List<JoinModule> FULL_JOIN = List.of(JOIN_SERVICE_INFO, JOIN_RECORD_PENDINGS,
            JOIN_RECORD_PENDING_USER_INFO);

    private final DSLContext dsl;

    public Flux<RecordEntity> findFullByFilter(RecordFilter filter) {
        return this.findByFilter(filter, FULL_JOIN);
    }

    public Flux<RecordEntity> findByFilter(RecordFilter filter, List<JoinModule> joins) {
        var query = fetch(dsl, joins);
        var filteredQuery = buildFilter(query, filter);
        return RxUtils.fluxIterable(filteredQuery, this::tryToMapAll);
    }

    private Collection<RecordEntity> tryToMapAll(List<org.jooq.Record> it) {
        Map<Long, RecordEntity> records = new HashMap<>();
        it.forEach(rec -> {
            var recordEntity = records.computeIfAbsent(rec.get(recordTable.ID), k -> mapJustRecEntity(rec));
            var pending = rec.into(recordPendingTable.fields()).into(com.tlback.domain.RecordPending.class);
            if (pending != null && pending.getId() != null && pending.getPendingOwner() == null)
                pending.setPendingOwner(
                        rec.into(userTable.fields()).into(com.tlback.domain.DomainUserEntity.class));

            if (recordEntity.getServiceInfo() == null)
                recordEntity.setServiceInfo(rec.into(serviceInfoTable.fields()).into(ServiceInfoEntity.class));

            recordEntity.getRecordPendings().add(pending);
        });
        return records.values();
    }

    public static SelectConditionStep<org.jooq.Record> buildFilter(SelectJoinStep<org.jooq.Record> select,
            RecordFilter filter) {
        var query = select.where(DSL.noCondition());

        if (filter.getServiceInfoId() != null)
            query = query.and(recordTable.SERVICE_INFO_ID.eq(filter.getServiceInfoId()));

        if (filter.getRecordOwnerId() != null)
            query = query.and(recordTable.RECORD_OWNER_ID.eq(filter.getRecordOwnerId()));

        if (filter.getClientId() != null)
            query = query.and(recordPendingTable.CLIENT_ID.eq(filter.getClientId()));

        if (filter.getDateFrom() != null)
            query = query.and(recordTable.TS_FROM.greaterThan(filter.getDateFrom()));

        if (filter.getDateTo() != null)
            query = query.and(recordTable.TS_TO.lessThan(filter.getDateTo()));

        if (filter.getIsPublic() != null)
            query = query.and(recordTable.IS_PUBLIC.eq(filter.getIsPublic()));

        return query;
    }

    public static RecordEntity mapJustRecEntity(org.jooq.Record rec) {
        return rec.into(recordTable.fields()).map(mapper -> {
            var offsetTz = mapper.get(recordTable.TZ);
            var tz = ZoneOffset.of(offsetTz);

            var timeFrom = mapper.get(recordTable.TS_FROM);
            var timeTo = mapper.get(recordTable.TS_TO);

            var entity = new RecordEntity();
            entity.setId(rec.get(recordTable.ID));
            entity.setServiceInfoId(rec.get(recordTable.SERVICE_INFO_ID));
            entity.setRecordOwnerId(rec.get(recordTable.RECORD_OWNER_ID));
            entity.setIsPublic(rec.get(recordTable.IS_PUBLIC));
            entity.setTsFrom(timeFrom.atOffset(tz));
            entity.setTsTo(timeTo.atOffset(tz));
            entity.setTz(tz);
            return entity;
        });
    }

    public static SelectJoinStep<org.jooq.Record> basicFetch(DSLContext dsl) {
        return dsl.select().from(recordTable);
    }

    public static SelectJoinStep<org.jooq.Record> fetch(DSLContext dsl, List<JoinModule> joins) {
        var mainFetch = basicFetch(dsl);

        for (var join : joins)
            mainFetch = join.apply(mainFetch);

        return mainFetch;
    }
}
