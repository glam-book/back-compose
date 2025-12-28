package com.tlback.domain.dao.jooq.modules;

import org.jooq.DSLContext;

import reactor.core.publisher.Mono;

public interface InsertModule<R, K> {

   Mono<K> insert(R entity, DSLContext dsl);
}
