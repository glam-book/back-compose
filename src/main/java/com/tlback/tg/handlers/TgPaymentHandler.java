package com.tlback.tg.handlers;

import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TgPaymentHandler {

    void onSucessfulPayment(SuccessfulPayment payment);

    AnswerPreCheckoutQuery handlePrecheckQuery(PreCheckoutQuery query);

    void handleTelegramError(PreCheckoutQuery query, TelegramApiException e);

}
