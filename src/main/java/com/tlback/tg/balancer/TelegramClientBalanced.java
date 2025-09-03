package com.tlback.tg.balancer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TelegramClientBalanced {
    private final Integer TASK_LIMIT = 30;
    private final LinkedBlockingQueue<Runnable> buffer = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(30);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final TelegramClient tgClient;

    @PostConstruct
    public void initNotificationProcessing() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Runnable> tasks = new ArrayList<>(TASK_LIMIT);
                buffer.drainTo(tasks, TASK_LIMIT); // Забрать до 30 задач
                for (var task : tasks) {
                    executor.submit(() -> {
                        task.run();
                        return null;
                    });
                }
            } catch (Exception e) {
                log.error("⚠️ Error in scheduled task processor", e);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public <T extends Serializable> void execute(BotApiMethod<T> method,
            Consumer<TelegramApiException> tgErrorConsumer,
            Consumer<T> responseHandler) {
        Runnable runnable = () -> {
            try {
                var response = tgClient.execute(method);
                responseHandler.accept(response);
            } catch (TelegramApiException e) {
                tgErrorConsumer.accept(e);
            }
        };

        buffer.offer(runnable);
    }

    public <T extends Serializable> void execute(BotApiMethod<T> method) {
        this.execute(method, e -> {}, r -> {});
    }
}
