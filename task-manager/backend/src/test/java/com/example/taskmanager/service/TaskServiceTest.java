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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Spy
    private TaskMapper taskMapper = new TaskMapper();

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_persistsAndPublishesCreatedEvent() {
        TaskRequest request = new TaskRequest("Write tests", "JUnit + Mockito", TaskStatus.TODO);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });

        TaskResponse response = taskService.createTask(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Write tests");
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        verify(eventPublisher, times(1)).publishEvent(any(TaskCreatedEvent.class));
        verify(eventPublisher, never()).publishEvent(any(TaskCompletedEvent.class));
    }

    @Test
    void createTask_withDoneStatus_publishesCompletedEvent() {
        TaskRequest request = new TaskRequest("Finish", null, TaskStatus.DONE);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(2L);
            return task;
        });

        TaskResponse response = taskService.createTask(request);

        assertThat(response.completedAt()).isNotNull();
        verify(eventPublisher).publishEvent(any(TaskCreatedEvent.class));
        verify(eventPublisher).publishEvent(any(TaskCompletedEvent.class));
    }

    @Test
    void getTaskById_existing_returnsTask() {
        Task task = sampleTask(5L, "Existing", TaskStatus.IN_PROGRESS);
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.title()).isEqualTo("Existing");
    }

    @Test
    void getTaskById_missing_throwsNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllTasks_returnsMappedList() {
        when(taskRepository.findAll()).thenReturn(List.of(
                sampleTask(1L, "A", TaskStatus.TODO),
                sampleTask(2L, "B", TaskStatus.DONE)));

        List<TaskResponse> result = taskService.getAllTasks();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TaskResponse::title).containsExactly("A", "B");
    }

    @Test
    void updateTask_transitionToDone_publishesCompletedEventOnce() {
        Task existing = sampleTask(7L, "Old", TaskStatus.TODO);
        when(taskRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskRequest request = new TaskRequest("New title", "Updated", TaskStatus.DONE);
        TaskResponse response = taskService.updateTask(7L, request);

        assertThat(response.title()).isEqualTo("New title");
        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
        assertThat(response.completedAt()).isNotNull();

        ArgumentCaptor<TaskCompletedEvent> captor = ArgumentCaptor.forClass(TaskCompletedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertThat(captor.getValue().taskId()).isEqualTo(7L);
    }

    @Test
    void deleteTask_existing_deletes() {
        Task existing = sampleTask(3L, "ToDelete", TaskStatus.TODO);
        when(taskRepository.findById(3L)).thenReturn(Optional.of(existing));

        taskService.deleteTask(3L);

        verify(taskRepository).delete(existing);
    }

    @Test
    void deleteTask_missing_throwsNotFound() {
        when(taskRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(404L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(taskRepository, never()).delete(any());
    }

    private Task sampleTask(Long id, String title, TaskStatus status) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setStatus(status);
        return task;
    }
}
