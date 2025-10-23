package com.tlback.tg.handlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.message.Message;

import com.tlback.tg.balancer.TelegramClientGroupping;


public abstract class TgAbsMessageHandler implements TgMessageHandler {
    private Map<String, CommandHandler> commandHandler;

    public static interface CommandHandler {
        void handle(Message cmd, TelegramClientGroupping tgClient);
    }

    public TgAbsMessageHandler(Map<String, CommandHandler> commandHandler) {
        this.commandHandler = commandHandler;
    }

    public TgAbsMessageHandler() {
        this.commandHandler = registerCommandHandlers();
    }

    protected abstract Map<String, CommandHandler> registerCommandHandlers();

    @Override
    public void onMessage(Message msg, TelegramClientGroupping tgClient) {
        if (msg.hasText()) {
            var chatId = msg.getChatId().toString();
            var txt = msg.getText();
            if (txt.startsWith("/")) {
                var commandHandler = this.commandHandler.get(txt);
                if (commandHandler != null)
                    commandHandler.handle(msg, tgClient);
            } else {
                handleSimpleText(txt, chatId, tgClient);
            }
        }
    }

    protected abstract void handleSimpleText(String txt, String chatId, TelegramClientGroupping tgClient);

}
