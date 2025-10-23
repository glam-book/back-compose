package com.tlback.tg;

import java.util.Optional;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import com.tlback.core.model.DomainUserEntity;
import com.tlback.core.model.TelegramUser;
import com.tlback.notifier.impl.NotificationAdapter;
import com.tlback.notifier.model.NotificationAttachment;
import com.tlback.tg.balancer.PerfProps;
import com.tlback.tg.balancer.TelegramClientGroupping;

import io.vavr.CheckedConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TelegramNotifierAdapter extends NotificationAdapter<DomainUserEntity> {
    private final TelegramClientGroupping client;

    private InputFile buildInputFile(NotificationAttachment attachment) {
        return new InputFile(attachment.getInputStream(), attachment.getFileName());
    }

    @Override
    protected void sendMessage(DomainUserEntity user, String message) {
        var tgUser = user.getTgUser();
        tgUserHandler(tgUser, tgId -> {
            var msg = new SendMessage(tgId, message);
            client.executeGeneric(PerfProps.of(tgId), msg);
        });
    }

    @Override
    protected void photoHandler(DomainUserEntity user, NotificationAttachment att) {
        var tgUser = user.getTgUser();
        tgUserHandler(tgUser, tgId -> {
            var sendPhoto = SendPhoto.builder();
            sendPhoto.chatId(tgId);
            sendPhoto.photo(buildInputFile(att));

            if (att.hasAttamentText())
                sendPhoto.caption(att.getAttachmentText());

            client.executeGeneric(PerfProps.of(tgId), sendPhoto.build());
        });
    }

    @Override
    protected void defaultHandler(DomainUserEntity user, NotificationAttachment att) {
        var tgUser = user.getTgUser();
        tgUserHandler(tgUser, tgId -> {
            var sendDocument = SendDocument.builder();
            sendDocument.chatId(tgId);
            sendDocument.document(buildInputFile(att));

            if (att.hasAttamentText())
                sendDocument.caption(att.getAttachmentText());

            client.executeGeneric(PerfProps.of(tgId), sendDocument.build());
        });
    }

    private void tgUserHandler(Optional<TelegramUser> user, CheckedConsumer<String> action) {
        var opt = user.map(it -> it.getId());

        opt.ifPresentOrElse(tgId -> {
            var tgIdString = tgId.toString();
            try {
                action.accept(tgIdString);
            } catch (Throwable e) {
                log.warn("Exception while notifing telegram user {}", e);
            }
        }, () -> log.warn("No user present to send notification, or something went wrong: {}", user));
    }
}
