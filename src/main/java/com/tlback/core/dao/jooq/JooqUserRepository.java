package com.tlback.core.dao.jooq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.springframework.stereotype.Service;

import com.tlback.core.dao.jooq.modules.JoinModule;
import com.tlback.core.model.DomainUserEntity;
import com.tlback.core.model.TelegramUser;
import com.tlback.core.tools.RxUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class JooqUserRepository {
    private final DSLContext dsl;
    private static final com.tlback.jooq.gen.tables.TelegramUser telegramUserTable = com.tlback.jooq.gen.tables.TelegramUser.TELEGRAM_USER;
    private static final com.tlback.jooq.gen.tables.DomainUser userTable = com.tlback.jooq.gen.tables.DomainUser.DOMAIN_USER;

    public static final JoinModule SUB_JOINS = dsl -> dsl
                .leftJoin(telegramUserTable)
                .on(userTable.ID.eq(telegramUserTable.USER_ID));

    public Mono<DomainUserEntity> findById(Long id) {
        var query = fetchWhere(userTable.ID.eq(id));
        log.info(query.toString());

        return Mono.from(flux(query));
    }

    public Mono<DomainUserEntity> findByTgId(Long tgId) {
        var query = fetchWhere(telegramUserTable.ID.eq(tgId));
        log.info(query.toString());

        return Mono.from(flux(query));
    }

    public Flux<DomainUserEntity> findByIds(Long... ids) {
        var query = fetchWhere(userTable.ID.in(ids));
        return flux(query);
    }

    public Flux<DomainUserEntity> findByIds(List<Long> ids) {
        var query = fetchWhere(userTable.ID.in(ids));
        return flux(query);
    }

    public Flux<DomainUserEntity> findAll(int offset, int limit) {
        var query = fetch(dsl).offset(offset).limit(limit);
        return flux(query);
    }

    public Flux<DomainUserEntity> findAll() {
        var query = fetch(dsl);
        return flux(query);
    }

    private Flux<DomainUserEntity> flux(Select<org.jooq.Record> query) {
        return RxUtils.fluxIterable(query, it -> collectToMap(it).values());
    }

    public static Map<Long, DomainUserEntity> collectToMap(Iterable<org.jooq.Record> records) {
        Map<Long, DomainUserEntity> users = new HashMap<>();
        records.forEach(rec -> {
            users.computeIfAbsent(rec.get(userTable.ID), k -> mapUser(rec));
        });
        return users;
    }

    public static DomainUserEntity mapUser(org.jooq.Record rec) {
        var user = rec.into(userTable.fields()).into(DomainUserEntity.class);
        var tgUser = rec.into(telegramUserTable.fields()).into(TelegramUser.class);
        user.setTgUser(Optional.ofNullable(tgUser));
        return user;
    }

    public static SelectJoinStep<org.jooq.Record> fetch(DSLContext dsl) {
        var select = dsl.select(userTable.fields()).select(telegramUserTable.fields())
                .from(userTable);
        return SUB_JOINS.apply(select);
    }

    private SelectConditionStep<org.jooq.Record> fetchWhere(Condition where) {
        return fetch(dsl).where(where);
    }

    public Mono<TelegramUser> createTelegramUser(Long userId, TelegramUser tgUser) {
        var tgInsertQuery = dsl.insertInto(telegramUserTable)
                .columns(telegramUserTable.USER_ID, telegramUserTable.ID, telegramUserTable.FIRST_NAME,
                        telegramUserTable.LAST_NAME, telegramUserTable.USERNAME, telegramUserTable.LANGUAGE_CODE,
                        telegramUserTable.IS_BOT, telegramUserTable.IS_PREMIUM,
                        telegramUserTable.ADDED_TO_ATTACHMENT_MENU, telegramUserTable.ALLOWS_WRITE_TO_PM,
                        telegramUserTable.PHOTO_URL)
                .values(userId, tgUser.getId(), tgUser.getFirstName(), tgUser.getLastName(), tgUser.getUsername(),
                        tgUser.getLanguageCode(), tgUser.getIsBot(), tgUser.getIsPremium(),
                        tgUser.getAddedToAttachmentMenu(), tgUser.getAllowsWriteToPm(), tgUser.getPhotoUrl())
                .returningResult(telegramUserTable.fields());

        return Mono.from(tgInsertQuery)
                .map(it -> it.into(telegramUserTable.fields()).into(TelegramUser.class));
    }

    public Mono<DomainUserEntity> save(DomainUserEntity entity) {
        var insertQuery = dsl.insertInto(userTable)
                .columns(userTable.LOGIN, userTable.NAME, userTable.LAST_NAME, userTable.MIDDLE_NAME, userTable.ICON)
                .values(entity.getLogin(), entity.getName(), entity.getLastName(), entity.getMiddleName(), entity.getProfileIcon())
                .returningResult(userTable.fields());

        log.info(insertQuery.toString());

        // Сохраняем пользователя и получаем его id
        return Mono.from(insertQuery)
                .cache()
                .map(rec -> mapUser(rec))
                .flatMap(savedUser -> {
                    if (entity.getTgUser().isPresent()) {
                        var tgUser = entity.getTgUser().get();
                        // Сохраняем telegram_user с полученным userId
                        return createTelegramUser(savedUser.getId(), tgUser)
                                .map(tg -> {
                                    savedUser.setTgUser(Optional.of(tg));
                                    return savedUser;
                                });
                    } else {
                        return Mono.just(savedUser);
                    }
                });
    }
}
