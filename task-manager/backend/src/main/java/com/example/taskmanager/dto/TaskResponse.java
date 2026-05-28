package com.example.taskmanager.dto;

import com.example.taskmanager.domain.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponse(
                Long id,
                String title,
                String description,
                TaskStatus status,
                LocalDateTime createdAt,
                LocalDateTime completedAt)
{
}
