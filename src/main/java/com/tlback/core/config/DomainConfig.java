package com.tlback.core.config;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.tlback.core.model.DomainUserEntity;
import com.tlback.core.service.RecordService;
import com.tlback.core.service.UserService;
import com.tlback.events.core.EventPublisher;
import com.tlback.events.impl.SpringEventPublisher;
import com.tlback.events.impl.record.RecordEventHandler;
import com.tlback.events.impl.record.RecordUpdateHandler;
import com.tlback.notifier.UserNotifier;
import com.tlback.notifier.impl.AsyncDelegatingNotificationAdapter;
import com.tlback.notifier.impl.NotificationAdapter;
import com.tlback.tg.TelegramNotifierAdapter;
import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.balancer.TelegramClientImpl;

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

    @Bean
    RecordUpdateHandler recordUpdateHandler(TelegramClientGroupping tgClient, UserNotifier<DomainUserEntity> userNotifier, RecordService recordService) {
        return new RecordUpdateHandler(recordService, userNotifier);
    }

    @Bean
    TelegramClientGroupping tgClient(TelegramClient tgClient) {
        return new TelegramClientImpl(tgClient);
    }

    @Bean
    UserNotifier<DomainUserEntity> userNotifier(TelegramClientGroupping tgClient) {
        var tgAdapter = new TelegramNotifierAdapter(tgClient);
        List<NotificationAdapter<DomainUserEntity>> list = List.of(tgAdapter);
        var asyncAdapter = new AsyncDelegatingNotificationAdapter<>(list);
        return asyncAdapter;
    }
}
