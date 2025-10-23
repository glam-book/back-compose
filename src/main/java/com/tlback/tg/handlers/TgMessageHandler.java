package com.tlback.tg.handlers;

import org.telegram.telegrambots.meta.api.objects.message.Message;

import com.tlback.tg.balancer.TelegramClientGroupping;


public interface TgMessageHandler {
    static final String pattern = "";

    void onMessage(Message msg, TelegramClientGroupping tgClient);

    default String format(String msg) {
        StringBuilder result = new StringBuilder();
        
        for (char c : msg.toCharArray()) {
            if (c == '.' || c == ',' || c == '#' || c == '-') {
                result.append("\\").append(c);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}