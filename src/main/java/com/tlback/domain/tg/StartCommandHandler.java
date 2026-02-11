package com.tlback.domain.tg;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.handlers.TgCommandHandler;

@Component
public class StartCommandHandler implements TgCommandHandler {

    @Override
    public void handle(Message msg, TelegramClientGroupping tgClient) {
        var welcomeText = """
                🎉 Добро пожаловать в бот сервис для создания записей и заказов!
                Нажмите на иконку 'App' в левом нижнем углу, чтобы открыть приложение.
                """;
        var sendMessage = SendMessage.builder().chatId(msg.getChatId())
                .text(welcomeText)
                .build();
        tgClient.executeGeneric(sendMessage);
    }

    @Override
    public String getRouting() {
        return "/start";
    }

}
