package com.tlback.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcAuditing
@EnableR2dbcRepositories("com.tlback.jpa.repos")
public class R2DBCConfig {

    // @Bean
    // ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
    //     ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
    //     initializer.setConnectionFactory(connectionFactory);

    //     var populator = new CompositeDatabasePopulator();
    //     populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("db/sql/create-tables.sql")));
    //     initializer.setDatabasePopulator(populator);
    //     return initializer;
    // }
    
}
