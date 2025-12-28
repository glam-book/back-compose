package com.tlback.tg;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.handlers.TgMessageHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/tg/webhook")
@RequiredArgsConstructor
@Slf4j
public class TgWebhookBot {
    private final TelegramClientGroupping grouppingClient;
    private final List<TgMessageHandler> messageHandler;

    @PostMapping
    public ResponseEntity<Void> onUpdate(@RequestBody Update update) throws Exception {
        log.info("Accepting telegram update...");

        var msg = update.getMessage();

        // Обработка успешной оплаты
        if (msg != null && msg.hasSuccessfulPayment()) {
            log.info("Successful payment message received");
            grouppingClient.executeGeneric(SendMessage.builder()
                    .text("✅ Оплата прошла успешно!")
                    .chatId(msg.getChatId())
                    .build());

            var payment = msg.getSuccessfulPayment();
        }

        if (msg != null)
            messageHandler.forEach(msgHandler -> msgHandler.onMessage(msg, grouppingClient));

        return ResponseEntity.ok().build();
    }
}
