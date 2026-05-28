package com.example.taskmanager.event;

public record TaskCompletedEvent(Long taskId, String title)
{
}
