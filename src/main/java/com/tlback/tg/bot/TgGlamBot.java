package com.tlback.tg.bot;

import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.handlers.TgMessageHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TgGlamBot implements LongPollingSingleThreadUpdateConsumer, NotificationApi {

    private final TelegramClientGroupping telegramClient;
    private final TgMessageHandler messageHandler;

    @Override
    public void sendNotification(String chatId, String message) {
        var msg = new SendMessage(chatId, message);
        telegramClient.executeGeneric(msg);
    }

    @Override
    public void consume(Update update) {
        log.info("Accepting telegram update...");
        if (update.hasMessage()) {
            var msg = update.getMessage();

            if (msg != null) {
                messageHandler.onMessage(msg, telegramClient);
            }
        }
    }

}
