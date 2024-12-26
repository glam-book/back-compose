package com.tlback.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tlback.dao.jooq.JooqUserRepository;
import com.tlback.dao.r2dbc.UserRepository;
import com.tlback.domain.DomainUserEntity;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final JooqUserRepository jooqUserRepository;

    public Mono<DomainUserEntity> createUser(DomainUserEntity user) {
        return repo.save(user);
    }

    @Transactional(readOnly = true)
    public Mono<DomainUserEntity> findById(Long id) {
        return jooqUserRepository.findById(id);
    }
}
