package com.tlback.core.web.telegram;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.tlback.tg.bot.AppArgs;
import com.tlback.tg.bot.TgGlamBot;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TelegramWebhookController {
    private final TgGlamBot tgGlamBot;

    @PostMapping(AppArgs.TG_WEB_HOOK_PATH)
    public Mono<?> onUpdateReceived(@RequestBody Update update) {
        return Mono.justOrEmpty(tgGlamBot.onWebhookUpdateReceived(update));
    }
}
