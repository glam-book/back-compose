package com.tlback.tg;

import java.util.List;

import org.telegram.telegrambots.meta.api.objects.Update;

import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.handlers.TgCallbackQueryHandler;
import com.tlback.tg.handlers.TgMessageHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TgBot {

    private final TelegramClientGroupping grouppingClient;
    private final List<TgMessageHandler> messageHandler;
    private final List<TgCallbackQueryHandler> callbackQueryHandlers;

    public void onUpdate(Update update) {
        log.info("Accepting telegram update...");

        if (update.hasCallbackQuery()) {
            var callbackQuery = update.getCallbackQuery();
            callbackQueryHandlers.forEach(handler -> handler.handle(callbackQuery, grouppingClient));
        }

        var msg = update.getMessage();
        if (msg != null && msg.hasSuccessfulPayment()) {
        }

        if (msg != null)
            messageHandler.forEach(msgHandler -> msgHandler.onMessage(msg, grouppingClient));
    }
}
