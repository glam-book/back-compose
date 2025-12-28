package com.tlback.tg.handlers;

import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;

public interface TgPaymentHandler {

    void onSuccessPayment(SuccessfulPayment successfulPayment);
}
