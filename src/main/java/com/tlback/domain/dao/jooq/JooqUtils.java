package com.tlback.domain.dao.jooq;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.SelectJoinStep;
import org.jooq.Table;

import com.tlback.domain.dao.jooq.modules.JoinModule;

public class JooqUtils {
    
    public static SelectJoinStep<org.jooq.Record> basicFetch(DSLContext dsl, Table<?> table) {
        return dsl.select().from(table);
    }

    public static SelectJoinStep<org.jooq.Record> fetch(DSLContext dsl, Table<?> table, List<JoinModule> joins) {
        var mainFetch = basicFetch(dsl, table);

        for (var join : joins)
            mainFetch = join.apply(mainFetch);

        return mainFetch;
    }

}
