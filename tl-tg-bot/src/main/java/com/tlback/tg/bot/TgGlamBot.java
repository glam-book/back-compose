package com.tlback.tg.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TgGlamBot extends TelegramLongPollingBot implements NotificationApi {

    public TgGlamBot(String token) {
        super(token);
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

    @Override
    public void onUpdateReceived(Update update) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onUpdateReceived'");
    }

}
