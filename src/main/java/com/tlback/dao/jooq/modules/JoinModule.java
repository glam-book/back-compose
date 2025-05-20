package com.tlback.dao.jooq.modules;

import org.jooq.SelectJoinStep;

@FunctionalInterface
public interface JoinModule {
    SelectJoinStep<org.jooq.Record> apply(SelectJoinStep<org.jooq.Record> query);
}
