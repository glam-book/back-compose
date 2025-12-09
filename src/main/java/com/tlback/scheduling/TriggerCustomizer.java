package com.tlback.scheduling;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

public interface TriggerCustomizer<T extends TriggerBuilder<Trigger>> {

    void customize(Function<String, TriggerKey> triggerKeyMapper, T triggerBuilder);

    default void customize(String triggerGroup, T triggerBuilder) {
        customize(triggerName -> new TriggerKey(triggerName, triggerGroup), triggerBuilder);
    }

    default Optional<Trigger> customize(Function<String, TriggerKey> triggerKeyMapper, String triggerGroup, SchedulerFactoryBean fb, 
        Consumer<Throwable> exceptionHandler) {
        var key = triggerKeyMapper.apply(triggerGroup);
        try {
            var trigger = fb.getScheduler().getTrigger(key);
            return Optional.ofNullable(trigger);
        } catch (Exception e) {
            exceptionHandler.accept(e);
        }
        return Optional.empty();
    }
}
