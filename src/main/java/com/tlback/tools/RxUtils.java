package com.tlback.tools;

import java.util.List;
import java.util.function.Function;

import org.jooq.Record;
import org.jooq.Select;

import reactor.core.publisher.Flux;

public final class RxUtils {
    public static <T, R> Flux<R> fluxIterable(Select<Record> select, Function<List<Record>, Iterable<R>> mapper) {
        var r = Flux.from(select).collectList();
        var b = r.flatMapIterable(mapper);
        return b;
    }

}
