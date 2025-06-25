package com.tlback.tg.bot;

import org.springframework.context.annotation.Bean;

import com.github.kshashov.telegram.config.TelegramBotGlobalPropertiesConfiguration;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetWebhook;

public class TgBotConfiguration {

    private static final String BOT_TOKEN = "123456789:ABC-DEF...";
    private static final String BOT_USERNAME = "MyManualWebhookBot";
    private static final String WEBHOOK_URL = "https://your.domain.com/telegram";

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(BOT_TOKEN);
    }

    @Bean
    TelegramBotGlobalPropertiesConfiguration configuration() {
        return builder -> builder
            .configureBot(BOT_TOKEN, 
                botBuilder -> botBuilder
                    .useWebhook(new SetWebhook().url(WEBHOOK_URL)));
    }

}
