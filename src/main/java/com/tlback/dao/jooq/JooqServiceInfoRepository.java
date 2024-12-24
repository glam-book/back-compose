package com.tlback.dao.jooq;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.jooq.DSLContext;
import org.jooq.SelectOnConditionStep;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.tlback.dao.jooq.tools.RxUtils;
import com.tlback.domain.ServiceInfoEntity;
import com.tlback.domain.UserEntity;
import com.tlback.domain.utils.RecordSupplier;
import com.tlback.domain.utils.ServiceOwneraAware;
import com.tlback.domain.view.ServiceInfoView;
import com.tlback.jooq.gen.tables.Record;
import com.tlback.jooq.gen.tables.ServiceInfo;
import com.tlback.jooq.gen.tables.User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class JooqServiceInfoRepository {
    private static final ServiceInfo serviceInfoTable = ServiceInfo.SERVICE_INFO;
    private static final User userTable = User.USER;
    private static final com.tlback.jooq.gen.tables.Record recordTable = Record.RECORD;

    private final DSLContext dsl;

    public Flux<ServiceInfoEntity> findAllByUserId(Long userId) {
        var condition = userTable.ID.eq(userId);
        var query = fetchFull(dsl).where(condition);

        return RxUtils.fluxIterable(query, iter ->
            collectToMapWithUser(iter,
                records -> JooqUserRepository.collectToMap(records).values().iterator().next(),
                 ServiceInfoEntity.class)
            .values());
    }

    public Flux<ServiceInfoView> findAllViewByUserId(Long userId) {
        var condition = userTable.ID.eq(userId);
        var query = fetchFull(dsl).where(condition);

        return RxUtils.fluxIterable(query, iter -> collectToMapWithUser(iter, null,
         ServiceInfoView.class).values());
    }

    public static <T extends RecordSupplier & ServiceOwneraAware> Map<Long, T>
        collectToMapWithUser(Iterable<org.jooq.Record> records,
            @Nullable Function<Iterable<org.jooq.Record>, UserEntity> userMapper, Class<T> clazz) {

        Map<Long, T> entities = new HashMap<>();
        records.forEach(rec -> {
            var serviceInfo = entities.computeIfAbsent(rec.get(serviceInfoTable.ID),
                    k -> rec.into(serviceInfoTable.fields())
                            .into(clazz));

            serviceInfo.getRecords().add(rec.into(recordTable.fields())
                .into(com.tlback.domain.RecordEntity.class));

            if (userMapper != null && serviceInfo.getServiceOwner() == null) {
                var user = userMapper.apply(records);
                serviceInfo.setServiceOwner(user);
            }
        });
        return entities;
    }

    public static SelectOnConditionStep<org.jooq.Record> fetchFull(DSLContext dsl) {
        return dsl
            .select(JooqUserRepository.fetch(dsl).asMultiset())
            .select(serviceInfoTable.fields())
            .select(userTable.fields())
            .select(recordTable.fields())
            .from(serviceInfoTable)
            .join(userTable)
            .on(serviceInfoTable.SERVICE_OWNER_ID.eq(userTable.ID))
            .join(recordTable)
            .on(serviceInfoTable.ID.eq(recordTable.SERVICE_INFO_ID));
    }
}
