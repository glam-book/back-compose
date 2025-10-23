package com.tlback.tg.balancer;

import java.io.Serializable;
import java.util.function.Consumer;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramClientGroupping extends TelegramClient {

        public <T extends Serializable, Method extends BotApiMethod<T>> void executeGeneric(
                        PerfProps props, Method method);

        public void executeGeneric(PerfProps props, SendPhoto photo);

        public void executeGeneric(PerfProps props, SendDocument document);

        public <T extends Serializable, Method extends BotApiMethod<T>> void executeGeneric(
                        PerfProps props, Method method,
                        Consumer<TelegramApiException> tgErrorConsumer,
                        Consumer<T> responseConsumer);

        public void executeGeneric(PerfProps props, SendDocument doc,
                        Consumer<TelegramApiException> tgErrorConsumer,
                        Consumer<Message> responseConsumer);

        public void executeGeneric(PerfProps props, SendPhoto photo,
                        Consumer<TelegramApiException> tgErrorConsumer,
                        Consumer<Message> responseConsumer);
}
