package com.example.taskmanager.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TaskEventListener
{
        private static final Logger LOG = LoggerFactory.getLogger(TaskEventListener.class);

        @EventListener
        public void onTaskCreated(TaskCreatedEvent event)
        {
                LOG.info("[EVENT] Task created -> id={}, title='{}'", event.taskId(), event.title());
        }

        @EventListener
        public void onTaskCompleted(TaskCompletedEvent event)
        {
                LOG.info("[EVENT] Task completed -> id={}, title='{}'", event.taskId(), event.title());
        }
}
