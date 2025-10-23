package com.tlback.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.handlers.TgMessageHandler;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class TelegramBotConfig {

    @Value("${GLAM_TG_BOT_TOKEN}")
    private String tgBotToken;

    @Bean
    TelegramClient telegramClient() {
        return new OkHttpTelegramClient(tgBotToken);
    }

}
