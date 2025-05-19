package com.tlback.tools;

import java.util.List;
import java.util.function.Function;

import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectOnConditionStep;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectOnCondition;

import reactor.core.publisher.Flux;

public final class RxUtils {

    private RxUtils() {
        // Private constructor to hide the implicit public one
    }

    public static <R> Flux<R> fluxIterable(Select<Record> select, Function<List<Record>, Iterable<R>> mapper) {
        return Flux.from(select).collectList().flatMapIterable(mapper);
    }

}
