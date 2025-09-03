package com.tlback.tg.handlers.impl;

import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.tlback.tg.handlers.TgAbsMessageHandler;

public class TgMessageHandlerImpl extends TgAbsMessageHandler {

    @Override
    protected Map<String, CommandHandler> registerCommandHandlers() {
        return Map.of();
    }

    @Override
    protected List<SendMessage> handleSimpleText(String txt, String chatId) {
        var msg1 = new SendMessage(chatId, "Ты уже проснулся ????");
        var msg2 = new SendMessage(chatId, "SOSAL ????");
        return List.of(msg1, msg2);
    }
    
}
