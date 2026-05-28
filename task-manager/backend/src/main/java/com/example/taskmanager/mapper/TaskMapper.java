package com.example.taskmanager.mapper;

import com.example.taskmanager.domain.Task;
import com.example.taskmanager.domain.TaskStatus;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper
{
        public Task toEntity(TaskRequest request)
        {
                Task task = new Task();
                task.setTitle(request.title());
                task.setDescription(request.description());
                task.setStatus(request.status() != null ? request.status() : TaskStatus.TODO);
                return task;
        }

        public TaskResponse toResponse(Task task)
        {
                return new TaskResponse(
                                task.getId(),
                                task.getTitle(),
                                task.getDescription(),
                                task.getStatus(),
                                task.getCreatedAt(),
                                task.getCompletedAt());
        }
}
