package com.tlback.app.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.tlback.tg.TgBot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tg/webhook")
@Slf4j
public class TgWebhookBot {

    private final TgBot tgBot;

    @PostMapping
    public ResponseEntity<Void> receiveUpdate(@RequestBody Update update) throws Exception {
        tgBot.onUpdate(update);
        return ResponseEntity.ok().build();
    }
}
