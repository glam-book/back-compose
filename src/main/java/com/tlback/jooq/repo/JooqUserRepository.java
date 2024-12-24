package com.tlback.jooq.repo;

import java.util.HashMap;
import java.util.Map;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOnConditionStep;
import org.springframework.stereotype.Service;

import com.tlback.jooq.gen.tables.Role;
import com.tlback.jooq.gen.tables.User;
import com.tlback.jooq.gen.tables.UserRole;
import com.tlback.jooq.repo.tools.RxUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class JooqUserRepository {
    private final DSLContext dsl;
    private final User userTable = User.USER;
    private final UserRole userRoleTable = UserRole.USER_ROLE;
    private final Role roleTable = Role.ROLE;

    public Mono<com.tlback.jpa.entities.User> findById(Long id) {
        var query = fetchWhere(userRoleTable.USER_ID.eq(id));
        return flux(query).next();
    }

    public Flux<com.tlback.jpa.entities.User> findByIds(Long...ids) {
        var query = fetchWhere(userRoleTable.USER_ID.in(ids));
        return flux(query);
    }

    public Flux<com.tlback.jpa.entities.User> findAll(int offset, int limit) {
        var query = fetch().offset(offset).limit(limit);
        return flux(query);
    }

    private Flux<com.tlback.jpa.entities.User> flux(Select<org.jooq.Record> query) {
        return RxUtils.fluxIterable(query,
                it -> {
                    Map<Long, com.tlback.jpa.entities.User> users = new HashMap<>();
                    it.stream().forEach(rec -> {
                        var user = users.computeIfAbsent(rec.get(userTable.ID),
                                k -> rec.into(userTable.fields()).into(com.tlback.jpa.entities.User.class));
                        user.getRoles().add(rec.into(roleTable.fields()).into(com.tlback.jpa.entities.Role.class));
                    });
                    return users.values();
                });
    }

    private SelectOnConditionStep<org.jooq.Record> fetch() {
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
        return fetch().where(where);
    }
}
