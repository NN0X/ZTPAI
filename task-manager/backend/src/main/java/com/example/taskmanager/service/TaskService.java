package com.example.taskmanager.service;

import com.example.taskmanager.domain.AppUser;
import com.example.taskmanager.domain.Task;
import com.example.taskmanager.domain.TaskStatus;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.event.TaskCompletedEvent;
import com.example.taskmanager.event.TaskCreatedEvent;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService
{
        private final TaskRepository taskRepository;
        private final UserRepository userRepository;
        private final TaskMapper taskMapper;
        private final ApplicationEventPublisher eventPublisher;

        public TaskService(TaskRepository taskRepository,
                        UserRepository userRepository,
                        TaskMapper taskMapper,
                        ApplicationEventPublisher eventPublisher)
        {
                this.taskRepository = taskRepository;
                this.userRepository = userRepository;
                this.taskMapper = taskMapper;
                this.eventPublisher = eventPublisher;
        }

        @Transactional(readOnly = true)
        public List<TaskResponse> getAllTasks(String username)
        {
                AppUser owner = findOwnerOrThrow(username);
                return taskRepository.findByOwner(owner).stream()
                                .map(taskMapper::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public TaskResponse getTaskById(Long id, String username)
        {
                return taskMapper.toResponse(findOwnedTaskOrThrow(id, username));
        }

        @Transactional
        public TaskResponse createTask(TaskRequest request, String username)
        {
                AppUser owner = findOwnerOrThrow(username);

                Task task = taskMapper.toEntity(request);
                task.setOwner(owner);
                if (task.getStatus() == TaskStatus.DONE)
                {
                        task.setCompletedAt(LocalDateTime.now());
                }

                Task saved = taskRepository.save(task);

                eventPublisher.publishEvent(new TaskCreatedEvent(saved.getId(), saved.getTitle()));
                if (saved.getStatus() == TaskStatus.DONE)
                {
                        eventPublisher.publishEvent(new TaskCompletedEvent(saved.getId(), saved.getTitle()));
                }
                return taskMapper.toResponse(saved);
        }

        @Transactional
        public TaskResponse updateTask(Long id, TaskRequest request, String username)
        {
                Task task = findOwnedTaskOrThrow(id, username);
                TaskStatus previousStatus = task.getStatus();

                task.setTitle(request.title());
                task.setDescription(request.description());
                if (request.status() != null)
                {
                        task.setStatus(request.status());
                }

                boolean justCompleted = task.getStatus() == TaskStatus.DONE && previousStatus != TaskStatus.DONE;
                if (justCompleted)
                {
                        task.setCompletedAt(LocalDateTime.now());
                }

                Task saved = taskRepository.save(task);

                if (justCompleted)
                {
                        eventPublisher.publishEvent(new TaskCompletedEvent(saved.getId(), saved.getTitle()));
                }
                return taskMapper.toResponse(saved);
        }

        @Transactional
        public void deleteTask(Long id, String username)
        {
                Task task = findOwnedTaskOrThrow(id, username);
                taskRepository.delete(task);
        }

        private Task findOwnedTaskOrThrow(Long id, String username)
        {
                AppUser owner = findOwnerOrThrow(username);
                return taskRepository.findByIdAndOwner(id, owner)
                                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        }

        private AppUser findOwnerOrThrow(String username)
        {
                return userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }
}
