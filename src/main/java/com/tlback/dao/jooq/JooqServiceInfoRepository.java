package com.tlback.dao.jooq;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.jooq.DSLContext;
import org.jooq.SelectOnConditionStep;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.tlback.jooq.gen.tables.DomainUser;
import com.tlback.jooq.gen.tables.Record;
import com.tlback.jooq.gen.tables.ServiceInfo;
import com.tlback.jooq.gen.tables.records.ServiceInfoRecord;
import com.tlback.model.DomainUserEntity;
import com.tlback.model.ServiceInfoEntity;
import com.tlback.model.utils.RecordSupplier;
import com.tlback.model.utils.ServiceOwneraAware;
import com.tlback.tools.RxUtils;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class JooqServiceInfoRepository {
    private static final ServiceInfo serviceInfoTable = ServiceInfo.SERVICE_INFO;
    private static final DomainUser userTable = DomainUser.DOMAIN_USER;
    private static final Record recordTable = Record.RECORD;

    private final DSLContext dsl;

    public Flux<ServiceInfoEntity> findAllByUserId(Long userId) {
        var condition = userTable.ID.eq(userId);
        var query = fetchFull(dsl).where(condition);

        return RxUtils.fluxIterable(query,
                iter -> collectToMapWithUser(iter,
                        records -> JooqUserRepository.collectToMap(records).values().iterator().next(),
                        ServiceInfoEntity.class).values());
    }

    public static <T extends RecordSupplier & ServiceOwneraAware> Map<Long, T> collectToMapWithUser(
            Iterable<org.jooq.Record> records,
            @Nullable Function<Iterable<org.jooq.Record>, DomainUserEntity> userMapper, Class<T> clazz) {

        Map<Long, T> entities = new HashMap<>();
        records.forEach(rec -> {
            var serviceInfo = entities.computeIfAbsent(rec.get(serviceInfoTable.ID),
                    k -> rec.into(serviceInfoTable.fields()).into(clazz));

            serviceInfo.getRecords().add(rec.into(recordTable.fields()).into(com.tlback.model.RecordEntity.class));

            if (userMapper != null && serviceInfo.getServiceOwner() == null) {
                var user = userMapper.apply(records);
                serviceInfo.setServiceOwner(user);
            }
        });
        return entities;
    }

    public static Mono<ServiceInfoRecord> insert(ServiceInfoEntity entity, DSLContext dsl) {
        return Mono.from(dsl.insertInto(serviceInfoTable)
                .set(serviceInfoTable.SERVICE_NAME, entity.getServiceName())
                .set(serviceInfoTable.TIME_DURATION, entity.getTimeDuration())
                .set(serviceInfoTable.SERVICE_DESCRIPTION, entity.getServiceDescription())
                .onDuplicateKeyUpdate()
                .set(serviceInfoTable.SERVICE_NAME, entity.getServiceName())
                .set(serviceInfoTable.TIME_DURATION, entity.getTimeDuration())
                .set(serviceInfoTable.SERVICE_DESCRIPTION, entity.getServiceDescription())
                .returningResult(serviceInfoTable.fields()))
                .map(it -> it.into(ServiceInfoRecord.class));
    }

    public static SelectOnConditionStep<org.jooq.Record> fetchFull(DSLContext dsl) {
        return dsl.select(JooqUserRepository.fetch(dsl).asMultiset()).select(serviceInfoTable.fields())
                .select(userTable.fields()).select(recordTable.fields()).from(serviceInfoTable).join(userTable)
                .on(serviceInfoTable.SERVICE_OWNER_ID.eq(userTable.ID)).join(recordTable)
                .on(serviceInfoTable.ID.eq(recordTable.SERVICE_INFO_ID));
    }
}
