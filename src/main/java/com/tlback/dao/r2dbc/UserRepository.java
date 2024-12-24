package com.tlback.dao.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.tlback.domain.UserEntity;


public interface UserRepository extends R2dbcRepository<UserEntity, Long> {
}
