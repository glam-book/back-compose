package com.tlback.domain.service.confirm;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import com.tlback.domain.model.PendingState;
import com.tlback.domain.model.TelegramUser;
import com.tlback.domain.model.contact.UserContactType;
import com.tlback.domain.model.utils.PendingConfirmInfo;
import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.handlers.TgCallbackQueryHandler;
import com.tlback.tg.handlers.TgCommandHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactTgConfirmAction implements ContactPendingConfirmAction<TelegramUser>,
        TgCommandHandler, TgCallbackQueryHandler {

    private final TelegramClientGroupping tgClient;
    private final PendingConfirmationService pendingConfirmationService;

    public static final String MESSAGE_ACTION = "/CONFIRMATION_REPLY";

    @Override
    public void sendConfirmRequest(TelegramUser targetContact, PendingConfirmInfo rec) {
        var message = SendMessage.builder();
        message.chatId(targetContact.getId());
        message.text(rec.toConfirmationMessage());

        var markupInline = InlineKeyboardMarkup.builder();
        List<InlineKeyboardRow> rowsInline = new ArrayList<>();

        var row = new InlineKeyboardRow();

        var yesButton = InlineKeyboardButton.builder();
        yesButton.text("🆗 Да, приду");
        yesButton.callbackData(MESSAGE_ACTION + ":" + rec.pendingId() + ":YES");

        var noButton = InlineKeyboardButton.builder();
        noButton.text("🚫 Не приду");
        noButton.callbackData(MESSAGE_ACTION + ":" + rec.pendingId() + ":NO");

        row.add(yesButton.build());
        row.add(noButton.build());
        rowsInline.add(row);

        markupInline.keyboard(rowsInline);
        message.replyMarkup(markupInline.build());

        tgClient.executeGeneric(message.build());
    }

    @Override
    public UserContactType supports() {
        return UserContactType.TG;
    }

    @Override
    public void handle(String data, TelegramClientGroupping tgClient) {
        var splitted = data.split(":");

        if (splitted.length > 0) {
            var recPendingId = Long.parseLong(splitted[0]);
            var answer = splitted[1];
            var state = toPendingState(answer);
            switch (state) {
                case CONFIRMED -> pendingConfirmationService.onPendingConfirmed(recPendingId)
                        .subscribeOn(Schedulers.boundedElastic());
                case CANCELLED -> pendingConfirmationService.onPendingCancelled(recPendingId)
                        .subscribeOn(Schedulers.boundedElastic());
            }
        }
    }

    private PendingState toPendingState(String answer) {
        if (answer.equals("YES"))
            return PendingState.CONFIRMED;
        else if (answer.equals("NO"))
            return PendingState.CANCELLED;
        return PendingState.CREATED;
    }

    @Override
    public String getRouting() {
        return MESSAGE_ACTION;
    }

    @Override
    public void handle(CallbackQuery callbackQuery, TelegramClientGroupping tgClient) {
        try {
            var data = callbackQuery.getData();
            log.info("Receving tg callback data: {} : full {} ", data, callbackQuery);

            var splitted = data.split(":");
            if (splitted.length > 0) {
                var recPendingId = Long.parseLong(splitted[1]);
                var answer = splitted[2];
                var state = toPendingState(answer);
                switch (state) {
                    case CONFIRMED ->
                        pendingConfirmationService.onPendingConfirmed(recPendingId)
                            .doOnSuccess(it -> tgClient.executeGeneric(buildEditMessageText(callbackQuery, "Ваша запись подверждена!")))
                            .subscribeOn(Schedulers.boundedElastic());
                    case CANCELLED -> 
                        pendingConfirmationService.onPendingCancelled(recPendingId)
                            .doOnSuccess(it -> tgClient.executeGeneric(buildEditMessageText(callbackQuery, "Ваша запись будет отменена")));
                }
            }
        } catch (Exception e) {
            sendErrorAnser(callbackQuery, e);
        }
    }

    private void sendErrorAnser(CallbackQuery q, Throwable e) {
        log.error("Error while confirming pedning: {}", e.getMessage(), e);
        var error = buildEditMessageText(q, "Произошла ошибка, свяжитесь с поддержкой");
        tgClient.executeGeneric(error);
    }

    private EditMessageText buildEditMessageText(CallbackQuery callbackQuery, String text) {
        var msg = callbackQuery.getMessage();
        log.info("Building answer callback message: {}", msg);

        return EditMessageText.builder()
                .chatId(msg.getChatId())
                .text(text)
                .messageId(msg.getMessageId())
                .build();
    }

}
