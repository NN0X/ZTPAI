package com.example.taskmanager.service;

import com.example.taskmanager.domain.Task;
import com.example.taskmanager.domain.TaskStatus;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.event.TaskCompletedEvent;
import com.example.taskmanager.event.TaskCreatedEvent;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business logic for the Task entity. Publishes domain events on
 * meaningful state transitions.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;

    public TaskService(TaskRepository taskRepository,
                       TaskMapper taskMapper,
                       ApplicationEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        return taskMapper.toResponse(findTaskOrThrow(id));
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Task task = taskMapper.toEntity(request);
        if (task.getStatus() == TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        }

        Task saved = taskRepository.save(task);

        eventPublisher.publishEvent(new TaskCreatedEvent(saved.getId(), saved.getTitle()));
        if (saved.getStatus() == TaskStatus.DONE) {
            eventPublisher.publishEvent(new TaskCompletedEvent(saved.getId(), saved.getTitle()));
        }
        return taskMapper.toResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findTaskOrThrow(id);
        TaskStatus previousStatus = task.getStatus();

        task.setTitle(request.title());
        task.setDescription(request.description());
        if (request.status() != null) {
            task.setStatus(request.status());
        }

        boolean justCompleted = task.getStatus() == TaskStatus.DONE && previousStatus != TaskStatus.DONE;
        if (justCompleted) {
            task.setCompletedAt(LocalDateTime.now());
        }

        Task saved = taskRepository.save(task);

        if (justCompleted) {
            eventPublisher.publishEvent(new TaskCompletedEvent(saved.getId(), saved.getTitle()));
        }
        return taskMapper.toResponse(saved);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = findTaskOrThrow(id);
        taskRepository.delete(task);
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }
}
