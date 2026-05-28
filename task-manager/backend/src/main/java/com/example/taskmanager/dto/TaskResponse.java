package com.example.taskmanager.dto;

import com.example.taskmanager.domain.TaskStatus;

import java.time.LocalDateTime;

/**
 * Outgoing representation of a task.
 */
public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {
}
