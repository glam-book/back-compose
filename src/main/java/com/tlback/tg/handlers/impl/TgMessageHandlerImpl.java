package com.tlback.tg.handlers.impl;

import java.util.Map;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.handlers.TgAbsMessageHandler;

public class TgMessageHandlerImpl extends TgAbsMessageHandler {

    @Override
    protected Map<String, CommandHandler> registerCommandHandlers() {
        return Map.of();
    }

    @Override
    protected void handleSimpleText(String txt,
            String chatId, TelegramClientGroupping tgClient) {
        var msg1 = new SendMessage(chatId, format("""
                Добро пожаловать в `glam bot` 🌱
                - Что делает приложение
                ```java
                public class A {
                    ## 
                }
                ```
                Сервис представляет собой CRM-систему для автоматизации и управления бизнесом в сфере услуг. 
                Он объединяет учет, склад, статистику, работу с клиентами в одном месте. 
                `Glam` формирует отчеты для бизнеса в 20 разрезах, чтобы руководители принимали решения на основании точных данных.
                """));
        msg1.enableMarkdownV2(true);
        tgClient.executeGeneric(msg1);
    }

}
