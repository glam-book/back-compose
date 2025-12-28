package com.tlback.domain.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.tlback.domain.model.DomainUserEntity;
import com.tlback.domain.notifier.AsyncDelegatingNotificationAdapter;
import com.tlback.domain.notifier.NotificationAdapter;
import com.tlback.domain.notifier.TelegramNotifierAdapter;
import com.tlback.domain.notifier.api.UserNotifier;
import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.balancer.TelegramClientImpl;

@Configuration
public class DomainConfig {

    @Bean
    TelegramClientGroupping tgClient(TelegramClient tgClient) {
        return new TelegramClientImpl(tgClient);
    }

    @Bean
    UserNotifier<DomainUserEntity> userNotifier(TelegramClientGroupping tgClient) {
        var tgAdapter = new TelegramNotifierAdapter(tgClient);
        List<NotificationAdapter<DomainUserEntity>> list = List.of(tgAdapter);
        return new AsyncDelegatingNotificationAdapter<>(list);
    }
}
