package com.tlback.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EntityScan("com.tlback.domain")
@EnableJpaRepositories("com.tlback.repos")
@EnableTransactionManagement
public class DomainConfig {
}
