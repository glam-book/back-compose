package owpk.ogovpn.domain.tg;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import owpk.ogovpn.tg.balancer.PerfProps;
import owpk.ogovpn.tg.balancer.TelegramClientGroupping;
import owpk.ogovpn.tg.handlers.TgMessageHandler;
import owpk.ogovpn.tg.handlers.TgPaymentHandler;

@RestController
@RequestMapping("/tg/webhook")
@RequiredArgsConstructor
@Slf4j
public class TgWebhookBot {
    private final TelegramClientGroupping grouppingClient;
    private final TgPaymentHandler paymentHandler;
    private final TgMessageHandler messageHandler;

    @PostMapping
    public ResponseEntity<Void> onUpdate(@RequestBody Update update) throws Exception {
        log.info("Accepting telegram update...");
        if (update.hasPreCheckoutQuery()) {
            var preCheck = update.getPreCheckoutQuery();
            var answer = paymentHandler.handlePrecheckQuery(preCheck);

            grouppingClient.executeGeneric(PerfProps.of(PerfProps.PrioriySelector.MAX), answer, e -> {
                log.error("Error while answering payment pre check query", e);
            }, r -> {
                log.info("Success telegram response after answering pre check query");
            });
        }

        var msg = update.getMessage();

        // Обработка успешной оплаты
        if (msg != null && msg.hasSuccessfulPayment()) {
            log.info("Successful payment message received");
            grouppingClient.executeGeneric(SendMessage.builder()
                .text("✅ Оплата прошла успешно!")
                .chatId(msg.getChatId())
                .build());

            var payment = msg.getSuccessfulPayment();
            paymentHandler.onSucessfulPayment(payment);
        }

        if (msg != null)
            messageHandler.onMessage(msg, grouppingClient);

        return ResponseEntity.ok().build();
    }
}


