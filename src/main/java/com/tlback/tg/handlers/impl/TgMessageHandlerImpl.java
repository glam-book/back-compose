package com.tlback.tg.handlers.impl;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.handlers.TgMessageHandler;

@Component
public class TgMessageHandlerImpl implements TgMessageHandler {

    @Override
    public void onMessage(Message msg, TelegramClientGroupping tgClient) {

    }
    
}
