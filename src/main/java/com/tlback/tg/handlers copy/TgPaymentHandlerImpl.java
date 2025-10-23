package owpk.ogovpn.domain.tg.handlers;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.RequiredArgsConstructor;
import owpk.ogovpn.domain.enums.PaymentStatus;
import owpk.ogovpn.domain.enums.PaymentType;
import owpk.ogovpn.domain.service.PaymentService;
import owpk.ogovpn.domain.service.SubscriptionService;
import owpk.ogovpn.tg.handlers.TgPaymentHandler;

@RequiredArgsConstructor
@Service
public class TgPaymentHandlerImpl implements TgPaymentHandler {
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;

    private static record PaymentEntry(UUID subId, PaymentType type) {
    }

    @Override
    public void onSucessfulPayment(SuccessfulPayment payment) {
        var invoice = payment.getInvoicePayload();
        paymentService.updateStatus(invoice, PaymentStatus.SUCCESS);
        paymentService.setExternalOperationId(invoice, payment.getProviderPaymentChargeId());
        paymentService.findByLabel(invoice)
                .map(it -> new PaymentEntry(it.getPk().getSubId(), it.getType()))
                .ifPresentOrElse(it -> {
                    subscriptionService.activatePayed(it.subId(), it.type());
                }, () -> {
                    // TODO Добавить обработку ошибок и ретрай очереди в случае если не удалось
                    // обновить подписку
                });
    }

    @Override
    public AnswerPreCheckoutQuery handlePrecheckQuery(PreCheckoutQuery query) {
        var label = query.getInvoicePayload();
        paymentService.updateStatus(label, PaymentStatus.PENDING);
        return AnswerPreCheckoutQuery.builder()
                .preCheckoutQueryId(query.getId())
                .ok(true)
                .build();
    }

    @Override
    public void handleTelegramError(PreCheckoutQuery query, TelegramApiException e) {
        var label = query.getInvoicePayload();
        paymentService.updateStatus(label, PaymentStatus.PENDING);
    }

}
