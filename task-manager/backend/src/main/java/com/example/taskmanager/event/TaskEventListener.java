package com.example.taskmanager.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for task domain events. In a real system this could trigger
 * notifications, metrics, audit logging, etc. Here it simply logs.
 */
@Component
public class TaskEventListener {

    private static final Logger log = LoggerFactory.getLogger(TaskEventListener.class);

    @EventListener
    public void onTaskCreated(TaskCreatedEvent event) {
        log.info("[EVENT] Task created -> id={}, title='{}'", event.taskId(), event.title());
    }

    @EventListener
    public void onTaskCompleted(TaskCompletedEvent event) {
        log.info("[EVENT] Task completed -> id={}, title='{}'", event.taskId(), event.title());
    }
}
