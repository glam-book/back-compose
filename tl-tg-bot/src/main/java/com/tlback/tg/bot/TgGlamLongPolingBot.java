package com.tlback.tg.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TgGlamLongPolingBot extends TelegramLongPollingBot implements NotificationApi {

    public TgGlamLongPolingBot(String token) {
        super(token);
    }

    @Override
    public void onUpdateReceived(Update update) {
        var incomingMsg = update.getMessage();
        var chatId = incomingMsg.getChatId();
        var msgText = incomingMsg.getText();
        log.info("ChatId: " + chatId + " Msg: " + msgText);
        var sendMessage = new SendMessage(chatId.toString(), "Hello from Glam Bot!");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "Glam Bot";
    }

    @Override
    public void sendNotification(String chatId, String message) {
        log.info("ChatId: " + chatId + " Msg: " + message);
        var sendMessage = new SendMessage(chatId, message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
