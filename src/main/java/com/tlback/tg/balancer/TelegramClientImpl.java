package com.tlback.tg.balancer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
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
    private final PriorityBlockingQueue<PriorityGroupEntry> buffer = 
        new PriorityBlockingQueue<>(DEFAULT_BUFFER_CAPACITY);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Consumer<TelegramApiException> defaultErrorHandler = e -> log.error("Telegram api exception: ", e);
    private final TelegramClient tgClient;

    private final ExecutorService executor;

    @PostConstruct
    public void initNotificationProcessing() {
        scheduler.scheduleAtFixedRate(() -> {
            consumeTasks(TASK_LIMIT);
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void consumeTasks(int limit) {
        try {
            int taskLimit = Math.max(0, limit - counter.get());

            if (taskLimit == 0) {
                log.warn("Reached 0 task limit, check counter! Maybe some task is freezed");
                return;
            }

            List<PriorityGroupEntry> tasks = new ArrayList<>(taskLimit);
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
        defaultSend(() -> tgClient.execute(method), defaultErrorHandler, r -> {
        });
    }

    @Override
    public void executeGeneric(SendPhoto photo) {
        defaultSend(() -> tgClient.execute(photo), defaultErrorHandler, r -> {
        });
    }

    @Override
    public void executeGeneric(SendDocument document) {
        defaultSend(() -> tgClient.execute(document), defaultErrorHandler, r -> {
        });
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
    public <T extends Serializable, Method extends BotApiMethod<T>> void executeGeneric(
            PerfProps props, Method method) {
        defaultSend(props, () -> tgClient.execute(method), defaultErrorHandler, r -> {
        });
    }

    @Override
    public void executeGeneric(PerfProps props, SendPhoto photo) {
        defaultSend(props, () -> tgClient.execute(photo), defaultErrorHandler, r -> {
        });
    }

    @Override
    public void executeGeneric(PerfProps props, SendDocument document) {
        defaultSend(props, () -> tgClient.execute(document), defaultErrorHandler, r -> {
        });
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> void executeGeneric(
            PerfProps props, Method method,
            Consumer<TelegramApiException> tgErrorConsumer,
            Consumer<T> responseConsumer) {
        defaultSend(props, () -> tgClient.execute(method), tgErrorConsumer, responseConsumer);
    }

    @Override
    public void executeGeneric(PerfProps props, SendDocument doc,
            Consumer<TelegramApiException> tgErrorConsumer,
            Consumer<Message> responseConsumer) {
        defaultSend(props, () -> tgClient.execute(doc), tgErrorConsumer, responseConsumer);
    }

    @Override
    public void executeGeneric(PerfProps props, SendPhoto photo,
            Consumer<TelegramApiException> tgErrorConsumer,
            Consumer<Message> responseConsumer) {
        defaultSend(props, () -> tgClient.execute(photo), tgErrorConsumer, responseConsumer);
    }

    public interface MessageSupplier<T> {
        T get() throws TelegramApiException;
    }

    private <T> void defaultSend(
            PerfProps props,
            MessageSupplier<T> messageSupplier,
            Consumer<TelegramApiException> tgErrorConsumer,
            Consumer<T> responseConsumer) {

        CheckedRunnable runnable = () -> {
            try {
                counter.incrementAndGet();
                log.info("Send telegram request: {}", props);
                var rs = messageSupplier.get();
                responseConsumer.accept(rs);
            } catch (TelegramApiException e) {
                log.warn("Exception on telegram requst: {}, {}", props, e.getLocalizedMessage());
                tgErrorConsumer.accept(e);
            } finally {
                counter.decrementAndGet();
            }
        };

        var groupEntry = new PriorityGroupEntry(props.groupId(), props.prioriy(), runnable);
        var offered = buffer.offer(groupEntry, 100, TimeUnit.MILLISECONDS);
        if (!offered) {
            log.warn("Telegram buffer full, rejecting message for props {}", props);
            // signal error to caller
            tgErrorConsumer.accept(new TelegramApiException("Buffer is full"));
        }
    }

    private <T> void defaultSend(
            MessageSupplier<T> messageSupplier,
            Consumer<TelegramApiException> tgErrorConsumer,
            Consumer<T> responseConsumer) {

        this.defaultSend(PerfProps.of(), messageSupplier, tgErrorConsumer, responseConsumer);
    }

    public record GroupEntry(String id, CheckedRunnable action) {
        public GroupEntry(CheckedRunnable action) {
            this(UNGROUPED_STRING, action);
        }
    }

    public record PriorityGroupEntry(String id, int priority, CheckedRunnable action)
            implements Comparable<PriorityGroupEntry> {
        public PriorityGroupEntry(CheckedRunnable action) {
            this(UNGROUPED_STRING, 0, action);
        }

        @Override
        public int compareTo(PriorityGroupEntry o) {
            if (o == null)
                return 1;
            return Integer.compare(this.priority, o.priority);
        }
    }
}
