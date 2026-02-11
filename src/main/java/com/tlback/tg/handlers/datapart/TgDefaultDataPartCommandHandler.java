package com.tlback.tg.handlers.datapart;

import org.telegram.telegrambots.meta.api.objects.message.Message;

import com.tlback.tg.balancer.TelegramClientGroupping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TgDefaultDataPartCommandHandler implements TgDataPartHandler {

    @Override
    public void handleDataPart(String data, Message msg, TelegramClientGroupping tgClient) {
        var prefix = data.charAt(0) + "";

        switch (prefix) {
            case SimpleDataPart.PREFIX -> {
                var simpleDataPart = SimpleDataPart.of(data);
                handleDataPart(simpleDataPart, msg, tgClient);
            }
            case KeyValueDataPart.PREFIX -> {
                var keyValueDataPart = KeyValueDataPart.of(data);
                handleDataPart(keyValueDataPart, msg, tgClient);
            }
            case TemplateDataPart.PREFIX -> {
                var templateDataPart = TemplateDataPart.of(data);
                handleDataPart(templateDataPart, msg, tgClient);
            }
            default ->
                log.warn("Unknow data type prefix: {}", prefix);
        }
    }

    @Override
    public void handleDataPart(DataPart dataPart, Message msg, TelegramClientGroupping tgClient) {
        if (dataPart instanceof SimpleDataPart sdp) {
            handleDataPart(sdp, msg, tgClient);
        } else if (dataPart instanceof KeyValueDataPart kvdp) {
            handleDataPart(kvdp, msg, tgClient);
        } else if (dataPart instanceof TemplateDataPart tdp) {
            handleDataPart(tdp, msg, tgClient);
        }
    }

    protected abstract void handleDataPart(SimpleDataPart dataParts, Message message,
            TelegramClientGroupping tgClient);

    protected abstract void handleDataPart(KeyValueDataPart dataParts, Message message,
            TelegramClientGroupping tgClient);

    protected abstract void handleDataPart(TemplateDataPart dataParts, Message message,
            TelegramClientGroupping tgClient);

}
