package com.tlback.domain.tg;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import com.tlback.domain.model.PendingState;
import com.tlback.domain.model.TelegramUser;
import com.tlback.domain.model.contact.UserContactType;
import com.tlback.domain.model.utils.PendingConfirmInfo;
import com.tlback.domain.service.confirm.ContactPendingConfirmAction;
import com.tlback.domain.service.confirm.PendingConfirmationService;
import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.handlers.TgCallbackQueryHandler;
import com.tlback.tg.handlers.datapart.KeyValueDataPart;
import com.tlback.tg.handlers.datapart.SimpleDataPart;
import com.tlback.tg.handlers.datapart.TemplateDataPart;
import com.tlback.tg.handlers.datapart.TgDataPartHandler;
import com.tlback.tg.handlers.datapart.TgDefaultDataPartCommandHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactTgConfirmAction extends TgDefaultDataPartCommandHandler 
    implements ContactPendingConfirmAction<TelegramUser>, TgCallbackQueryHandler {

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
        var yesComeCmd = TgDataPartHandler.buildCommand(MESSAGE_ACTION, SimpleDataPart.of(rec.pendingId(), "YES"));
        yesButton.callbackData(yesComeCmd);

        var noButton = InlineKeyboardButton.builder();
        noButton.text("🚫 Не приду");
        var notComeCmd = TgDataPartHandler.buildCommand(MESSAGE_ACTION, SimpleDataPart.of(rec.pendingId(), "NO"));
        noButton.callbackData(notComeCmd);

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
                    default -> throw new IllegalArgumentException("Unexpected value: " + state);
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

    @Override
    protected void handleDataPart(SimpleDataPart dataParts, Message message, TelegramClientGroupping tgClient) {
        var parts = dataParts.parts();
        if (!parts.isEmpty() && parts.size() >= 2) {
            var recPendingId = Long.parseLong(parts.get(0));
            var answer = parts.get(1);
            var state = toPendingState(answer);
            switch (state) {
                case CONFIRMED -> pendingConfirmationService.onPendingConfirmed(recPendingId)
                        .subscribeOn(Schedulers.boundedElastic());
                case CANCELLED -> pendingConfirmationService.onPendingCancelled(recPendingId)
                        .subscribeOn(Schedulers.boundedElastic());
                default -> throw new IllegalArgumentException("Unexpected value: " + state);
            }
        } else {
            throw new IllegalArgumentException("Unexpected contact confirm action parts value: " + parts);
        }
    }

    @Override
    protected void handleDataPart(KeyValueDataPart dataParts, Message message, TelegramClientGroupping tgClient) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleDataPart'");
    }

    @Override
    protected void handleDataPart(TemplateDataPart dataParts, Message message, TelegramClientGroupping tgClient) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleDataPart'");
    }

    @Override
    public void handle(Message msg, TelegramClientGroupping tgClient) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }

}
