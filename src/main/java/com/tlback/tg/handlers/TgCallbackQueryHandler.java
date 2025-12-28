package com.tlback.tg.handlers;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import com.tlback.tg.balancer.TelegramClientGroupping;

public interface TgCallbackQueryHandler {
    
    void handle(CallbackQuery callbackQuery, TelegramClientGroupping tgClient);
}
