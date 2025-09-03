package com.tlback.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import com.tlback.tg.balancer.TelegramClientBalanced;
import com.tlback.tg.bot.TgGlamBot;
import com.tlback.tg.handlers.TgMessageHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TelegramBotLifecycle implements SmartLifecycle {

    private final TelegramClientBalanced telegramClient;
    private final TgMessageHandler messageHandler;
    private volatile boolean running = false;

    @Value("${GLAM_TG_BOT_TOKEN}")
    private String tgBotToken;

    private TelegramBotsLongPollingApplication botsApplication;

    public TelegramBotLifecycle(TelegramClientBalanced telegramClient,
            TgMessageHandler messageHandler) {
        this.telegramClient = telegramClient;
        this.messageHandler = messageHandler;
    }

    @Override
    public void start() {
        try {
            botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(tgBotToken, new TgGlamBot(telegramClient, messageHandler));
            running = true;
            log.info("Telegram bot started");
        } catch (Exception e) {
            log.error("Error registering bot", e);
        }
    }

    @Override
    public void stop() {
        try {
            if (botsApplication != null) {
                botsApplication.close();
                log.info("Telegram bot stopped");
            }
        } catch (Exception e) {
            log.error("Error closing bot", e);
        } finally {
            running = false;
        }
    }

    @Override
    public boolean isAutoStartup() {
        return true; // бот запустится сам после старта контекста
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
