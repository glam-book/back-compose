package com.tlback.tg.handlers;

import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public abstract class TgAbsMessageHandler implements TgMessageHandler {
    private Map<String, CommandHandler> commandHandler;

    public static interface CommandHandler {
        List<SendMessage> handle(Message cmd);
    }

    public TgAbsMessageHandler(Map<String, CommandHandler> commandHandler) {
        this.commandHandler = commandHandler;
    }

    public TgAbsMessageHandler() {
        this.commandHandler = registerCommandHandlers();
    }

    protected abstract Map<String, CommandHandler> registerCommandHandlers();

    @Override
    public List<SendMessage> onMessage(Message msg) {
        if (msg.hasText()) {
            var chatId = msg.getChatId().toString();
            var txt = msg.getText();
            if (txt.startsWith("/")) {
                var commandHandler = this.commandHandler.get(txt);
                if (commandHandler != null)
                    return commandHandler.handle(msg);
            } else {
                return handleSimpleText(txt, chatId);
            }
        }
        return List.of();
    }

    protected abstract List<SendMessage> handleSimpleText(String txt, String chatId);

}
