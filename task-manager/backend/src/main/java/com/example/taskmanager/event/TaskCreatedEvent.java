package com.example.taskmanager.event;

/**
 * Published when a new task is created.
 */
public record TaskCreatedEvent(Long taskId, String title) {
}
