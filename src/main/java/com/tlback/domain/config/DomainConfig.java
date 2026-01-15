package com.tlback.domain.config;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.tlback.domain.model.DomainUserEntity;
import com.tlback.domain.notifier.AsyncDelegatingNotificationAdapter;
import com.tlback.domain.notifier.NotificationAdapter;
import com.tlback.domain.notifier.TelegramNotifierAdapter;
import com.tlback.domain.notifier.api.UserNotifier;
import com.tlback.tg.TgBot;
import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.balancer.TelegramClientImpl;
import com.tlback.tg.handlers.TgCallbackQueryHandler;
import com.tlback.tg.handlers.TgMessageHandler;

@Configuration
public class DomainConfig {

    @Bean
    TelegramClientGroupping tgClient(TelegramClient tgClient, ExecutorService executorService) {
        return new TelegramClientImpl(tgClient, executorService);
    }

    @Bean
    ExecutorService executorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    UserNotifier<DomainUserEntity> userNotifier(TelegramClientGroupping tgClient, ExecutorService executorService) {
        var tgAdapter = new TelegramNotifierAdapter(tgClient);
        List<NotificationAdapter<DomainUserEntity>> list = List.of(tgAdapter);
        return new AsyncDelegatingNotificationAdapter<>(list, executorService);
    }

    @Bean
    TgBot tgBot(TelegramClientGroupping tgClient,
            List<TgMessageHandler> messageHandlers,
            List<TgCallbackQueryHandler> callbackQueryHandlers) {
        return new TgBot(tgClient, messageHandlers, callbackQueryHandlers);
    }
}
