package com.tlback.tg.handlers;

import java.util.List;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface TgMessageHandler {

    List<SendMessage> onMessage(Message msg);
}