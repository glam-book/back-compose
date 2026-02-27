package com.tlback.domain.config;

import org.jooq.DSLContext;
import org.jooq.Log;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.tools.JooqLogger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy;

import io.r2dbc.spi.ConnectionFactory;

@Configuration
public class JooqConfig {

    @Bean
    @Primary
    DSLContext dslContext(org.jooq.Configuration jooqConfiguration) {
        return DSL.using(jooqConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean
    org.jooq.Configuration jooqConfiguration(ConnectionFactory connectionFactory) {
        suppressJooqWarningLogs();
        var transactionAwareDataSource = new TransactionAwareConnectionFactoryProxy(connectionFactory);

        return new DefaultConfiguration()
                .set(transactionAwareDataSource)
                .set(SQLDialect.POSTGRES)
                .set(jooqSettings());
    }

    private void suppressJooqWarningLogs() {
        JooqLogger.globalThreshold(Log.Level.ERROR);
    }

    // Configure jOOQ settings
    private Settings jooqSettings() {
        return new Settings()
                .withFetchWarnings(false)
                .withExecuteLogging(false)
                .withRenderNameCase(RenderNameCase.LOWER)
                .withRenderQuotedNames(RenderQuotedNames.ALWAYS)
                .withParseDialect(SQLDialect.POSTGRES);
    }
}
