package com.tlback.tg.handlers.impl;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.tlback.tg.handlers.TgPaymentHandler;

@Component
public class TgPaymentHandlerImpl implements TgPaymentHandler {

    @Override
    public void onSucessfulPayment(SuccessfulPayment payment) {
    }

    @Override
    public AnswerPreCheckoutQuery handlePrecheckQuery(PreCheckoutQuery query) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handlePrecheckQuery'");
    }

    @Override
    public void handleTelegramError(PreCheckoutQuery query, TelegramApiException e) {
    }
    
}
