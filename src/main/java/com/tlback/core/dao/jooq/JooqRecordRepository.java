package com.tlback.core.dao.jooq;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import com.tlback.core.common.daofilter.RecordFilter;
import com.tlback.core.dao.jooq.modules.JoinModule;
import com.tlback.core.model.DomainUserEntity;
import com.tlback.core.model.RecordEntity;
import com.tlback.core.model.ServiceInfoEntity;
import com.tlback.core.tools.RxUtils;
import com.tlback.jooq.gen.tables.Record;
import com.tlback.jooq.gen.tables.RecordPending;
import com.tlback.jooq.gen.tables.ServiceInfo;
import com.tlback.jooq.gen.tables.records.RecordRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class JooqRecordRepository {
    public static final Record recordTable = Record.RECORD;
    public static final RecordPending recordPendingTable = RecordPending.RECORD_PENDING;
    public static final ServiceInfo serviceInfoTable = ServiceInfo.SERVICE_INFO;
    public static final com.tlback.jooq.gen.tables.DomainUser userTable = com.tlback.jooq.gen.tables.DomainUser.DOMAIN_USER;

    public static final JoinModule JOIN_SERVICE_INFO = mainFetch -> mainFetch.join(serviceInfoTable)
            .on(recordTable.SERVICE_INFO_ID.eq(serviceInfoTable.ID));

    public static final JoinModule JOIN_RECORD_PENDINGS = mainFetch -> mainFetch.leftJoin(recordPendingTable)
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

        log.info(filteredQuery.toString());
        return RxUtils.fluxIterable(filteredQuery, it -> this.tryToMapAll(it).values());
    }

    public Mono<RecordEntity> findById(Long id, JoinModule... joins) {
        return this.findById(id, Arrays.stream(joins).toList());
    }

    public Mono<RecordEntity> findById(Long id, List<JoinModule> joins) {
        var query = fetch(dsl, joins).where(recordTable.ID.eq(id));

        log.info(query.toString());
        return Mono.from(query).map(this::tryToMap);
    }

    /**
     * Проверить, что запись принадлежит пользователю
     */
    public Mono<Boolean> isOwner(long recordId, long userId) {
        return Mono.fromCallable(() -> dsl.fetchExists(
                dsl.selectOne()
                        .from(recordTable)
                        .where(recordTable.ID.eq(recordId))
                        .and(recordTable.RECORD_OWNER_ID.eq(userId))));
    }

    public static RecordRecord mapToRecord(RecordEntity entity) {
        var rec = new RecordRecord();
        rec.setId(entity.getId());
        rec.setRecordOwnerId(entity.getRecordOwnerId());
        rec.setIsPublic(entity.getIsPublic());
        rec.setTz(entity.getTz().toString());
        rec.setTsFrom(entity.getTsFrom().toLocalDateTime());
        rec.setTsTo(entity.getTsTo().toLocalDateTime());
        return rec;
    }

    public static Mono<RecordRecord> insert(RecordRecord recordEntity, DSLContext dsl) {
        var insertFields = new HashMap<>();
        insertFields.put(recordTable.SERVICE_INFO_ID, recordEntity.getServiceInfoId());
        insertFields.put(recordTable.RECORD_OWNER_ID, recordEntity.getRecordOwnerId());
        insertFields.put(recordTable.IS_PUBLIC, recordEntity.getIsPublic());
        insertFields.put(recordTable.TZ, recordEntity.getTz());
        insertFields.put(recordTable.TS_FROM, recordEntity.getTsFrom());
        insertFields.put(recordTable.TS_TO, recordEntity.getTsTo());

        var insert = dsl.insertInto(recordTable)
                .set(insertFields);

        var sql = insert
                .returningResult(recordTable.fields());

        log.info("Insert query: {}", sql.toString());
        return Mono.from(sql)
                .map(it -> it.into(RecordRecord.class));
    }

    private Map<Long, RecordEntity> tryToMapAll(List<org.jooq.Record> it) {
        var cache = new HashMap<Long, RecordEntity>();
        Function<org.jooq.Record, Long> id = rec -> rec.get(recordTable.ID);

        return it.stream()
                .filter(rec -> id.apply(rec) != null)
                .collect(Collectors.toMap(
                        id::apply,
                        rec -> cache.computeIfAbsent(id.apply(rec), ind -> tryToMap(rec)),
                        (rec1, rec2) -> {
                            rec1.getRecordPendings().addAll(rec2.getRecordPendings());
                            return rec1;
                        }));
    }

    private RecordEntity tryToMap(org.jooq.Record rec) {
        var recordEntity = mapJustRecEntity(rec);
        var pending = rec.into(recordPendingTable.fields()).into(com.tlback.core.model.RecordPending.class);
        if (pending != null && pending.getId() != null && pending.getPendingOwner() == null)
            pending.setPendingOwner(
                    rec.into(userTable.fields()).into(DomainUserEntity.class));

        if (recordEntity.getServiceInfo() == null)
            recordEntity.setServiceInfo(rec.into(serviceInfoTable.fields()).into(ServiceInfoEntity.class));

        recordEntity.getRecordPendings().add(pending);
        return recordEntity;
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

    public Mono<RecordEntity> update(RecordRecord record, JoinModule... joinModules) {
        // Обновляем запись по ID, возвращаем обновлённую сущность с нужными join-ами
        var update = dsl.update(recordTable)
                .set(recordTable.SERVICE_INFO_ID,
                        DSL.coalesce(DSL.val(record.getServiceInfoId()), recordTable.SERVICE_INFO_ID))
                .set(recordTable.RECORD_OWNER_ID,
                        DSL.coalesce(DSL.val(record.getRecordOwnerId()), recordTable.RECORD_OWNER_ID))
                .set(recordTable.IS_PUBLIC, DSL.coalesce(DSL.val(record.getIsPublic()), recordTable.IS_PUBLIC))
                .set(recordTable.TZ, DSL.coalesce(DSL.val(record.getTz()), recordTable.TZ))
                .set(recordTable.TS_FROM, DSL.coalesce(DSL.val(record.getTsFrom()), recordTable.TS_FROM))
                .set(recordTable.TS_TO, DSL.coalesce(DSL.val(record.getTsTo()), recordTable.TS_TO))
                .where(recordTable.ID.eq(record.getId()))
                .returning(recordTable.ID);

        return Mono.from(update)
                .map(r -> r.get(recordTable.ID))
                .flatMap(id -> findById(id, joinModules));
    }

    public Mono<RecordEntity> save(RecordRecord record, JoinModule... joinModules) {
        // Вставляем новую запись и возвращаем созданную сущность с нужными join-ами
        var insert = dsl.insertInto(recordTable)
                .set(recordTable.SERVICE_INFO_ID, record.getServiceInfoId())
                .set(recordTable.RECORD_OWNER_ID, record.getRecordOwnerId())
                .set(recordTable.IS_PUBLIC, record.getIsPublic())
                .set(recordTable.TZ, record.getTz())
                .set(recordTable.TS_FROM, record.getTsFrom())
                .set(recordTable.TS_TO, record.getTsTo())
                .returning(recordTable.ID);

        return Mono.from(insert)
                .map(r -> r.get(recordTable.ID))
                .flatMap(id -> findById(id, joinModules));
    }

    public Mono<Boolean> delete(Long id) {
        var deleteSql = dsl.delete(recordTable)
            .where(recordTable.ID.eq(id));
        return Mono.from(deleteSql).map(it -> it != 0);
    }
}
