package com.example.taskmanager.event;

public record TaskCreatedEvent(Long taskId, String title)
{
}
