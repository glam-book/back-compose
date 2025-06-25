package com.tlback.tg.bot;

import org.springframework.beans.factory.annotation.Value;

import com.github.kshashov.telegram.api.MessageType;
import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.bind.annotation.BotController;
import com.github.kshashov.telegram.api.bind.annotation.BotRequest;

@BotController
public class TlTelegramBot implements TelegramMvcController {

    @Value("${GLAM_TG_BOT_TOKEN}")
    private String tgBotToken;

    @Override
    public String getToken() {
        return tgBotToken;
    }

    @BotRequest(value = "/start", type = { MessageType.MESSAGE })
    public String start() {
        return "Привет! Это модульный бот.";
    }
}
