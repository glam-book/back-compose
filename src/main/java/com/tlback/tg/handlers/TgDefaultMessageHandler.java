package com.tlback.tg.handlers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.handlers.datapart.TgDataPartHandler;

import io.vavr.control.Try;

@Service
public class TgDefaultMessageHandler implements TgMessageHandler {
    private Map<String, TgCommandHandler> commandHandlerMap;

    public TgDefaultMessageHandler(List<TgCommandHandler> commandHandler) {
        this.commandHandlerMap = commandHandler.stream()
                .collect(Collectors.toMap(it -> it.getRouting(), Function.identity()));
    }

    @Override
    public void onMessage(Message msg, TelegramClientGroupping tgClient) {
        if (msg.hasText()) {
            var chatId = msg.getChatId().toString();
            var txt = msg.getText();
            if (txt.startsWith("/")) {
                var isDataAble = txt.contains(TgDataPartHandler.commandPartDelimiter());
                var cmd = isDataAble ? txt.substring(0, txt.indexOf(TgDataPartHandler.commandPartDelimiter())) : txt;
                var commandHandler = this.commandHandlerMap.get(cmd);
                if (commandHandler != null) {
                    if (isDataAble && commandHandler instanceof TgDataPartHandler tdp) {
                        var cmdData = Try.of(() -> txt.substring(txt.indexOf(TgDataPartHandler.commandPartDelimiter()) + 1)).getOrElse(txt);
                        tdp.handleDataPart(cmdData, msg, tgClient);
                    } else
                        commandHandler.handle(msg, tgClient);
                }
            } else {
                handleSimpleText(txt, chatId, tgClient);
            }
        }
    }

    protected void handleSimpleText(String txt, String chatId, TelegramClientGroupping tgClient) {
        // no op
    }

}
