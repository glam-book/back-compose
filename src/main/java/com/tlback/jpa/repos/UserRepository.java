package com.tlback.jpa.repos;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.tlback.jpa.entities.User;


public interface UserRepository extends R2dbcRepository<User, Long> {
}
