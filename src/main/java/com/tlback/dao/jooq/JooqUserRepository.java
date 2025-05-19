package com.tlback.dao.jooq;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOnConditionStep;
import org.springframework.stereotype.Service;

import com.tlback.domain.DomainUserEntity;
import com.tlback.jooq.gen.tables.DomainUser;
import com.tlback.jooq.gen.tables.TelegramUser;
import com.tlback.tools.RxUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class JooqUserRepository {
    private final DSLContext dsl;
    private static final TelegramUser telegramUserTable = TelegramUser.TELEGRAM_USER;
    private static final DomainUser userTable = DomainUser.DOMAIN_USER;

    public Mono<com.tlback.domain.DomainUserEntity> findById(Long id) {
        var query = fetchWhere(userTable.ID.eq(id));
        return Mono.from(flux(query));
    }

    public Flux<com.tlback.domain.DomainUserEntity> findByIds(Long... ids) {
        var query = fetchWhere(userTable.ID.in(ids));
        return flux(query);
    }

    public Flux<com.tlback.domain.DomainUserEntity> findAll(int offset, int limit) {
        var query = fetch(dsl).offset(offset).limit(limit);
        return flux(query);
    }

    private Flux<com.tlback.domain.DomainUserEntity> flux(Select<org.jooq.Record> query) {
        return RxUtils.fluxIterable(query, it -> collectToMap(it).values());
    }

    public static Map<Long, com.tlback.domain.DomainUserEntity> collectToMap(Iterable<org.jooq.Record> records) {
        Map<Long, com.tlback.domain.DomainUserEntity> users = new HashMap<>();
        records.forEach(rec -> {
            var user = users.computeIfAbsent(rec.get(userTable.ID),
                    k -> rec.into(userTable.fields()).into(com.tlback.domain.DomainUserEntity.class));
            if (user.getTgUser() == null) {
                var tgUser = rec.into(telegramUserTable.fields()).into(com.tlback.domain.TelegramUser.class);
                user.setTgUser(Optional.ofNullable(tgUser));
            }
            // user.getRoles().add(rec.into(roleTable.fields()).into(com.tlback.domain.RoleEntity.class));
        });
        return users;
    }

    public static SelectOnConditionStep<org.jooq.Record> fetch(DSLContext dsl) {
        return dsl.select(userTable.fields()).select(telegramUserTable.fields()).from(userTable).join(telegramUserTable)
                .on(userTable.ID.eq(telegramUserTable.USER_ID));
    }

    private SelectConditionStep<org.jooq.Record> fetchWhere(Condition where) {
        return fetch(dsl).where(where);
    }

    public Mono<com.tlback.domain.TelegramUser> createTelegramUser(Long userId, com.tlback.domain.TelegramUser tgUser) {
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
                .map(it -> it.into(telegramUserTable.fields()).into(com.tlback.domain.TelegramUser.class));
    }

    public Mono<DomainUserEntity> save(DomainUserEntity entity) {
        var insertQuery = dsl.insertInto(userTable)
                .columns(userTable.LOGIN, userTable.NAME, userTable.LAST_NAME, userTable.MIDDLE_NAME)
                .values(entity.getLogin(), entity.getName(), entity.getLastName(), entity.getMiddleName())
                .returningResult(userTable.fields());

        var initial = Flux.from(insertQuery).collectList().map(it -> collectToMap(it).values().iterator().next());

        if (entity.getTgUser().isPresent()) {
            var tgUser = entity.getTgUser().get();

            var i = initial.flatMap(it -> {
                var tgMono = createTelegramUser(it.getId(), tgUser);
                return tgMono.map(tg -> {
                    it.setTgUser(Optional.of(tg));
                    return it;
                });
            });
            return i;
        }
        return initial;
    }

    public Mono<DomainUserEntity> findByTgId(Long id) {
        return Mono.error(new UnsupportedOperationException());
    }
}
