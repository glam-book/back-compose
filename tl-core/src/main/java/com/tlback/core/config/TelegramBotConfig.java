package com.tlback.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.tlback.tg.bot.TgGlamBot;

@Configuration
public class TelegramBotConfig {

    @Value("${GLAM_TG_BOT_TOKEN}")
    private String botToken;

    @Value("${app.baseUrl}")
    private String appBaseUrl;

    @Bean
    TgGlamBot bot() {
        return new TgGlamBot(botToken, AppArgs.TG_WEB_HOOK_PATH);
    }

    @Bean
    TgBotInitializer botInitializer(TgGlamBot bot) {
        return new TgBotInitializer(bot, appBaseUrl + AppArgs.TG_WEB_HOOK_PATH);
    }

    public static class TgBotInitializer {
        private final TgGlamBot bot;
        private final String url;

        public TgBotInitializer(TgGlamBot bot, String url) {
            this.bot = bot;
            this.url = url;
        }

        @EventListener(ContextRefreshedEvent.class)
        public void init() throws Exception {
            var botApis = new TelegramBotsApi(DefaultBotSession.class);
            botApis.registerBot(bot, new SetWebhook(url));
        }
    }
}
