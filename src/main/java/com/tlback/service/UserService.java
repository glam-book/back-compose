package com.tlback.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tlback.jooq.repo.JooqUserRepository;
import com.tlback.jpa.entities.User;
import com.tlback.jpa.repos.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final JooqUserRepository jooqUserRepository;

    public Mono<User> createUser(User user) {
        return repo.save(user);
    }

    @Transactional(readOnly = true)
    public Mono<User> findById(Long id) {
        return jooqUserRepository.findById(id);
    }
}
