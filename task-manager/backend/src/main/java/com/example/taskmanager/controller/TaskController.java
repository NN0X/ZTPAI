package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController
{
        private final TaskService taskService;

        public TaskController(TaskService taskService)
        {
                this.taskService = taskService;
        }

        @GetMapping
        public List<TaskResponse> getAll(Authentication authentication)
        {
                return taskService.getAllTasks(authentication.getName());
        }

        @GetMapping("/{id}")
        public TaskResponse getById(@PathVariable Long id, Authentication authentication)
        {
                return taskService.getTaskById(id, authentication.getName());
        }

        @PostMapping
        public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request, Authentication authentication)
        {
                TaskResponse created = taskService.createTask(request, authentication.getName());
                return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }

        @PutMapping("/{id}")
        public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request, Authentication authentication)
        {
                return taskService.updateTask(id, request, authentication.getName());
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication)
        {
                taskService.deleteTask(id, authentication.getName());
                return ResponseEntity.noContent().build();
        }
}
