package com.tlback.core.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tlback.core.service.UserService;
import com.tlback.events.core.EventPublisher;
import com.tlback.events.impl.SpringEventPublisher;
import com.tlback.events.impl.record.RecordEventHandler;
import com.tlback.tg.balancer.TelegramClientGroupping;

@Configuration
public class DomainConfig {

    @Bean
    EventPublisher eventPublisher(ApplicationEventPublisher eventPublisher) {
        return new SpringEventPublisher(eventPublisher);
    }

    @Bean
    RecordEventHandler recordEventHandler(TelegramClientGroupping tgClient, UserService userService) {
        return new RecordEventHandler(tgClient, userService);
    }
}
