package com.tlback.dao.jooq.tools;

import java.util.List;
import java.util.function.Function;

import org.jooq.Insert;
import org.jooq.InsertReturningStep;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.impl.QOM.InsertReturning;

import reactor.core.publisher.Flux;

public final class RxUtils {
    public static <R> Flux<R> fluxIterable(Select<Record> select,
        Function<List<Record>, Iterable<R>> mapper) {
        var r  = Flux.from(select).collectList();
        return r.flatMapIterable(mapper);
    }
}
