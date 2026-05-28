package com.example.taskmanager.event;

/**
 * Published when a task transitions into the DONE state.
 */
public record TaskCompletedEvent(Long taskId, String title) {
}
