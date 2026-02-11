package com.tlback.tg.handlers;

import org.telegram.telegrambots.meta.api.objects.message.Message;

import com.tlback.tg.balancer.TelegramClientGroupping;

public interface TgCommandHandler {

    void handle(Message msg, TelegramClientGroupping tgClient);

    String getRouting();
}

