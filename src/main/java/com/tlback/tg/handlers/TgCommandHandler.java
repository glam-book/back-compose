package com.tlback.tg.handlers;

import com.tlback.tg.balancer.TelegramClientGroupping;

public interface TgCommandHandler {
    
    void handle(String commandData, TelegramClientGroupping tgClient);
    String getRouting();
}

