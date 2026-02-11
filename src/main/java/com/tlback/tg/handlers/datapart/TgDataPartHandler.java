package com.tlback.tg.handlers.datapart;

import org.telegram.telegrambots.meta.api.objects.message.Message;

import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.handlers.TgCommandHandler;

public interface TgDataPartHandler extends TgCommandHandler {

    public static String commandPartDelimiter() {
        return ":";
    }

    public static String buildCommand(String command, DataPart part) {
        return command + commandPartDelimiter() + part.convert();
    }

    void handleDataPart(DataPart dataPart, Message msg, TelegramClientGroupping tgClient);
    void handleDataPart(String rawaDataPart, Message msg, TelegramClientGroupping tgClient);
}
