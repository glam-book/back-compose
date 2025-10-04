package com.tlback.tg.balancer;

import java.io.Serializable;
import java.util.function.Consumer;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramClientGroupping extends TelegramClient {

    public <T extends Serializable, Method extends BotApiMethod<T>> void executeGeneric(String groupId, Method method);

    public void executeGeneric(String groupId, SendPhoto photo);

    public void executeGeneric(String groupId, SendDocument document);

    public <T extends Serializable, Method extends BotApiMethod<T>> void executeGeneric(String groupId, Method method,
            Consumer<TelegramApiException> tgErrorConsumer,
            Consumer<T> responseConsumer);

    public void executeGeneric(String groupId, SendDocument doc,
            Consumer<TelegramApiException> tgErrorConsumer,
            Consumer<Message> responseConsumer);

    public void executeGeneric(String groupId, SendPhoto photo,
            Consumer<TelegramApiException> tgErrorConsumer,
            Consumer<Message> responseConsumer);
}
