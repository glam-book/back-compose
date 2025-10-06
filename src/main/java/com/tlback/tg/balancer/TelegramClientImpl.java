package com.tlback.tg.balancer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import io.vavr.CheckedRunnable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TelegramClientImpl implements TelegramClientGroupping {
    private static final int TASK_LIMIT = 30;
    private static final String UNGROUPED_STRING = "ungrouped";

    private final AtomicInteger counter = new AtomicInteger();

    private static final int DEFAULT_BUFFER_CAPACITY = 1000;
    private final LinkedBlockingQueue<GroupEntry> buffer = new LinkedBlockingQueue<>(DEFAULT_BUFFER_CAPACITY);
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Consumer<TelegramApiException> defaultErrorHandler = e -> log.error("Telegram api exception: ", e);
    private final TelegramClient tgClient;

    @PostConstruct
    public void initNotificationProcessing() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int taskLimit = Math.max(0, TASK_LIMIT - counter.get());

                if (taskLimit == 0) {
                    log.warn("Reached 0 task limit, check counter! Maybe some task is freezed");
                    return;
                }

                List<GroupEntry> tasks = new ArrayList<>(taskLimit);
                buffer.drainTo(tasks, taskLimit);

                var grouped = tasks.stream().collect(Collectors.groupingBy(it -> it.id()));
                var ungrouped = grouped.remove(UNGROUPED_STRING);

                Consumer<CheckedRunnable> f = r -> {
                    try {
                        r.run();
                    } catch (Throwable e) {
                        log.warn("Error consuming telegram message action", e);
                    }
                };

                if (ungrouped != null)
                    ungrouped.forEach(it -> executor.submit(() -> f.accept(it.action())));

                grouped.entrySet().forEach(entry -> {
                    executor.submit(() -> {
                        var taskList = entry.getValue();
                        taskList.forEach(it -> f.accept(it.action()));
                    });
                });
            } catch (Exception e) {
                log.error("⚠️ Error in scheduled task processor", e);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdownExecutors() {
        try {
            scheduler.shutdownNow();
        } catch (Throwable t) {
            log.warn("Error while shutting down scheduler", t);
        }

        try {
            executor.shutdownNow();
        } catch (Throwable t) {
            log.warn("Error while shutting down executor", t);
        }
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> void executeGeneric(Method method) {
        defaultSend(() -> tgClient.execute(method), defaultErrorHandler, r -> {});
    }

    @Override
    public void executeGeneric(SendPhoto photo) {
        defaultSend(() -> tgClient.execute(photo), defaultErrorHandler, r -> {});
    }

    @Override
    public void executeGeneric(SendDocument document) {
        defaultSend(() -> tgClient.execute(document), defaultErrorHandler, r -> {});
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> void executeGeneric(Method method,
            Consumer<TelegramApiException> tgErrorConsumer, 
            Consumer<T> responseConsumer) {
        defaultSend(() -> tgClient.execute(method), tgErrorConsumer, responseConsumer);
    }

    @Override
    public void executeGeneric(SendDocument doc,
            Consumer<TelegramApiException> tgErrorConsumer, 
            Consumer<Message> responseConsumer) {
        defaultSend(() -> tgClient.execute(doc), tgErrorConsumer, responseConsumer);
    }

    @Override
    public void executeGeneric(SendPhoto photo,
            Consumer<TelegramApiException> tgErrorConsumer, 
            Consumer<Message> responseConsumer) {
        defaultSend(() -> tgClient.execute(photo), tgErrorConsumer, responseConsumer);
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> void executeGeneric(String groupId, Method method) {
        defaultSend(groupId, () -> tgClient.execute(method), defaultErrorHandler, r -> {});
    }

    @Override
    public void executeGeneric(String groupId, SendPhoto photo) {
        defaultSend(groupId, () -> tgClient.execute(photo), defaultErrorHandler, r -> {});
    }

    @Override
    public void executeGeneric(String groupId, SendDocument document) {
        defaultSend(groupId, () -> tgClient.execute(document), defaultErrorHandler, r -> {});
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> void executeGeneric(String groupId, Method method,
            Consumer<TelegramApiException> tgErrorConsumer, 
            Consumer<T> responseConsumer) {
        defaultSend(groupId, () -> tgClient.execute(method), tgErrorConsumer, responseConsumer);
    }

    @Override
    public void executeGeneric(String groupId, SendDocument doc,
            Consumer<TelegramApiException> tgErrorConsumer, 
            Consumer<Message> responseConsumer) {
        defaultSend(groupId, () -> tgClient.execute(doc), tgErrorConsumer, responseConsumer);
    }

    @Override
    public void executeGeneric(String groupId, SendPhoto photo,
            Consumer<TelegramApiException> tgErrorConsumer, 
            Consumer<Message> responseConsumer) {
        defaultSend(groupId, () -> tgClient.execute(photo), tgErrorConsumer, responseConsumer);
    }

    public interface MessageSupplier<T> {
        T get() throws TelegramApiException;
    }

    private <T> void defaultSend(String groupId, 
            MessageSupplier<T> messageSupplier, 
            Consumer<TelegramApiException> tgErrorConsumer, 
            Consumer<T> responseConsumer) {

        if (Objects.isNull(groupId))
            throw new IllegalArgumentException("Telegram message group id should not be null");

        CheckedRunnable runnable = () -> {
            try {
                counter.incrementAndGet();
                var rs = messageSupplier.get();
                responseConsumer.accept(rs);
            } catch (TelegramApiException e) {
                tgErrorConsumer.accept(e);
            } finally {
                counter.decrementAndGet();
            }
        };

        var groupEntry = new GroupEntry(groupId, runnable);
        try {
            var offered = buffer.offer(groupEntry, 100, TimeUnit.MILLISECONDS);
            if (!offered) {
                log.warn("Telegram buffer full, rejecting message for group {}", groupId);
                // signal error to caller
                tgErrorConsumer.accept(new TelegramApiException("Buffer is full"));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            tgErrorConsumer.accept(new TelegramApiException("Interrupted while offering to buffer"));
        }
    }

    private <T> void defaultSend(
            MessageSupplier<T> messageSupplier, 
            Consumer<TelegramApiException> tgErrorConsumer, 
            Consumer<T> responseConsumer) {

        this.defaultSend(UNGROUPED_STRING, messageSupplier, tgErrorConsumer, responseConsumer);
    }

    public record GroupEntry(String id, CheckedRunnable action) {
        public GroupEntry(CheckedRunnable action) {
            this(UNGROUPED_STRING, action);
        }
    }
}
