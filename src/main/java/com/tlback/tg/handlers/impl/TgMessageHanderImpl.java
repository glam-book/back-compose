package com.tlback.tg.handlers.impl;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.handlers.TgAbsMessageHandler;

@Component
public class TgMessageHanderImpl extends TgAbsMessageHandler {

    @Override
    protected Map<String, CommandHandler> registerCommandHandlers() {
        return Map.of("/start", startCommandHandler());
    }

    protected CommandHandler startCommandHandler() {
        var txt = """
                👋 Рады видеть вас в нашем боте!
                👉 Для начала нажмите кнопку ‘App’
                """;

        return (msg, client) -> {
            var tgMessage = SendMessage.builder()
                    .chatId(msg.getChatId())
                    .text(txt)
                    .build();

            client.executeGeneric(tgMessage);
        };
    }

    @Override
    protected void handleSimpleText(String txt, String chatId, TelegramClientGroupping tgClient) {
    }

}
