package com.tlback.dao.jooq;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.SelectOnConditionStep;
import org.springframework.stereotype.Service;

import com.tlback.dao.jooq.tools.RxUtils;
import com.tlback.domain.RecordEntity;
import com.tlback.jooq.gen.tables.Record;
import com.tlback.jooq.gen.tables.RecordPending;
import com.tlback.jooq.gen.tables.User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class JooqRecordRepository {
    private static final com.tlback.jooq.gen.tables.Record recordTable = Record.RECORD;
    private static final RecordPending recordPendingTable = RecordPending.RECORD_PENDING;
    private static final User userTable = User.USER;

    private final DSLContext dsl;

    public Flux<RecordEntity> findByDateAndServiceOwner(
            Long serviceOwnerId,
            Long dateStart,
            Long dateEnd,
            Optional<Boolean> isPublic) {
        var query = fetch(dsl)
                .where(recordTable.RECORD_OWNER_ID.eq(serviceOwnerId));
                // .and(recordTable.TIME_FROM.greaterThan(dateStart))
                // .and(recordTable.TIME_TO.lessThan(dateEnd));

        if (isPublic.isPresent())
            query = query.and(recordTable.IS_PUBLIC.eq(isPublic.get()));

        return RxUtils.fluxIterable(query, it -> {
            Map<Long, RecordEntity> records = new HashMap<>();
            it.forEach(rec -> {
                var record = records.computeIfAbsent(rec.get(recordTable.ID),
                        k -> rec.into(recordTable.fields()).into(RecordEntity.class));
                var pending = rec.into(recordPendingTable.fields())
                        .into(com.tlback.domain.RecordPending.class);
                if (pending.getPendingOwner() == null)
                    pending.setPendingOwner(rec.into(userTable.fields()).into(com.tlback.domain.UserEntity.class));
                record.getRecordPendings().add(pending);
            });
            return records.values();
        });
    }

    public static SelectOnConditionStep<org.jooq.Record> fetch(DSLContext dsl) {
        return dsl.select().from(recordTable)
                .join(recordPendingTable).on(recordTable.ID.eq(recordPendingTable.RECORD_ID))
                .join(userTable).on(recordPendingTable.CLIENT_ID.eq(userTable.ID));
    }
}
