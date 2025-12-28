package com.tlback.core.service.confirm;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import com.tlback.core.model.TelegramUser;
import com.tlback.core.model.contact.Supports;
import com.tlback.core.model.utils.PendingConfirmInfo;
import com.tlback.tg.balancer.TelegramClientGroupping;
import com.tlback.tg.handlers.TgCommandHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContactTgConfirmAction implements ContactPendingConfirmAction<TelegramUser>, TgCommandHandler {

    private final TelegramClientGroupping tgClient;
    private final RecordPendingConfirmationService confirmationService;

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
        yesButton.callbackData(MESSAGE_ACTION + ":" + rec.pendingId()+ ":YES");

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
    public Supports supports() {
        return Supports.TG;
    }

    @Override
    public void handle(String data, TelegramClientGroupping tgClient) {
        var splitted = data.split(":");

        if (splitted.length > 0) {
            var recPendingId = Long.parseLong(splitted[0]);
            var answer = splitted[1];
            confirmationService.confirm(recPendingId, answer.equals("YES"));
        }
    }

    @Override
    public String getRouting() {
        return MESSAGE_ACTION;
    }

}
