package com.tlback.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tlback.dao.jooq.JooqUserRepository;
import com.tlback.domain.DomainUserEntity;
import com.tlback.domain.TelegramUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final JooqUserRepository jooqUserRepository;

    @Transactional(readOnly = true)
    public Mono<DomainUserEntity> findById(Long id) {
        return jooqUserRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Mono<DomainUserEntity> findByTgId(Long id) {
        return jooqUserRepository.findById(id);
    }

    public Mono<DomainUserEntity> createFromTgUser(TelegramUser user) {
        var entity = new DomainUserEntity();
        entity.setTgUser(Optional.of(user));
        entity.setLogin(user.getUsername());
        entity.setName(user.getFirstName());
        entity.setLastName(user.getLastName());
        return jooqUserRepository.save(entity);
    }
}
