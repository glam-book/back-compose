package com.tlback.tg.bot;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TgGlamBot extends TelegramWebhookBot implements NotificationApi {
    private final String webHookPath;

    public TgGlamBot(String token, String webHookPath) {
        super(token);
        this.webHookPath = webHookPath;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        // Пример простой реализации: отправить приветственное сообщение в ответ на
        // любое обновление
        if (update.getMessage() != null && update.getMessage().getChatId() != null) {
            return new SendMessage(update.getMessage().getChatId().toString(), "Hello from Glam Bot!");
        }
        return null;
    }

    // Возвращаем путь вебхука
    @Override
    public String getBotPath() {
        return webHookPath;
    }

    @Override
    public String getBotUsername() {
        return "Glam Bot";
    }

    @Override
    public void sendNotification(String chatId, String message) {
        var msg = new SendMessage(chatId, message);
        try {
            super.execute(msg);
        } catch (TelegramApiException e) {
            log.error("Error sending message to chatId: {}", chatId, e);
        }
    }

}
