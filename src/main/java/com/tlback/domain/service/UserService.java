package com.tlback.domain.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.tlback.domain.abac.exception.NotFoundException;
import com.tlback.domain.dao.jooq.JooqUserRepository;
import com.tlback.domain.model.DomainUserEntity;
import com.tlback.domain.model.TelegramUser;

import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final JooqUserRepository jooqUserRepository;

    @Transactional(readOnly = true)
    public Mono<DomainUserEntity> findById(Long id) {
        return jooqUserRepository.findById(id)
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Mono<DomainUserEntity> findByTgId(Long id) {
        return jooqUserRepository.findByTgId(id)
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, label = "auth")
    public Mono<DomainUserEntity> createFromTgUser(TelegramUser user) {
        log.info("Creating default user from tg user: {}", user);
        var entity = new DomainUserEntity();
        entity.setTgUser(Option.of(user));
        entity.setLogin(user.getUsername());
        entity.setName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setProfileIcon(user.getPhotoUrl());
        return jooqUserRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Flux<DomainUserEntity> findByIds(List<Long> userIds) {
        return jooqUserRepository.findByIds(userIds);
    }
    public Flux<DomainUserEntity> findAll() {
        return jooqUserRepository.findAll();
    }
}
