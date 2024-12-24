package com.tlback.dao.jooq;

import java.util.HashMap;
import java.util.Map;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOnConditionStep;
import org.springframework.stereotype.Service;

import com.tlback.dao.jooq.tools.RxUtils;
import com.tlback.jooq.gen.tables.Role;
import com.tlback.jooq.gen.tables.User;
import com.tlback.jooq.gen.tables.UserRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class JooqUserRepository {
    private final DSLContext dsl;
    private static final User userTable = User.USER;
    private static final UserRole userRoleTable = UserRole.USER_ROLE;
    private static final Role roleTable = Role.ROLE;

    public Mono<com.tlback.domain.UserEntity> findById(Long id) {
        var query = fetchWhere(userRoleTable.USER_ID.eq(id));
        return flux(query).next();
    }

    public Flux<com.tlback.domain.UserEntity> findByIds(Long...ids) {
        var query = fetchWhere(userRoleTable.USER_ID.in(ids));
        return flux(query);
    }

    public Flux<com.tlback.domain.UserEntity> findAll(int offset, int limit) {
        var query = fetch(dsl).offset(offset).limit(limit);
        return flux(query);
    }

    private Flux<com.tlback.domain.UserEntity> flux(Select<org.jooq.Record> query) {
        return RxUtils.fluxIterable(query,
                it -> collectToMap(it).values());
    }

    public static Map<Long, com.tlback.domain.UserEntity> collectToMap(Iterable<org.jooq.Record> records) {
        Map<Long, com.tlback.domain.UserEntity> users = new HashMap<>();
        records.forEach(rec -> {
            var user = users.computeIfAbsent(rec.get(userTable.ID),
                    k -> rec.into(userTable.fields()).into(com.tlback.domain.UserEntity.class));
            user.getRoles().add(rec.into(roleTable.fields()).into(com.tlback.domain.RoleEntity.class));
        });
        return users;
    }

    public static SelectOnConditionStep<org.jooq.Record> fetch(DSLContext dsl) {
        return dsl
                .select(userTable.fields())
                .select(roleTable.fields())
                .from(userRoleTable)
                .join(userTable)
                .on(userTable.ID.eq(userRoleTable.USER_ID))
                .join(roleTable)
                .on(userRoleTable.ROLE_ID.eq(roleTable.ID));
    }

    private SelectConditionStep<org.jooq.Record> fetchWhere(Condition where) {
        return fetch(dsl).where(where);
    }
}
