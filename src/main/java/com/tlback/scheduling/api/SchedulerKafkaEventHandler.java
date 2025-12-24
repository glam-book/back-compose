package com.tlback.scheduling.api;

import java.util.Map;

public interface SchedulerKafkaEventHandler {

    void execute(String topic, String action, Map<String, Object> jobDataMap);

}
