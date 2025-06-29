package com.tlback.tg.bot;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.kshashov.telegram.config.TelegramBotGlobalPropertiesConfiguration;
import com.pengrad.telegrambot.request.SetWebhook;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

@Slf4j
@Configuration
public class TgBotConfiguration {
        private static final String WEBHOOK_URL = "https://tl.ow-pk.ru";

        @Value("${GLAM_TG_BOT_TOKEN}")
        private String token;

        @Bean
        public TelegramBotGlobalPropertiesConfiguration configure() {
                log.info("::::::: {}", token);
                var okHttp = new OkHttpClient.Builder()
                                .connectTimeout(12, TimeUnit.SECONDS)
                                .build();

                return builder -> builder
                                .configureBot(token, botBuilder -> botBuilder
                                                .useWebhook(new SetWebhook().url(WEBHOOK_URL))
                                                .configure(builder1 -> builder1.okHttpClient(okHttp)));
        }
}