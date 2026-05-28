package com.example.taskmanager.service;

import com.example.taskmanager.domain.AppUser;
import com.example.taskmanager.domain.Role;
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
import org.junit.jupiter.api.BeforeEach;
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
class TaskServiceTest
{
        private static final String USERNAME = "alice";

        @Mock
        private TaskRepository taskRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private ApplicationEventPublisher eventPublisher;

        @Spy
        private TaskMapper taskMapper = new TaskMapper();

        @InjectMocks
        private TaskService taskService;

        private AppUser owner;

        @BeforeEach
        void setUp()
        {
                owner = new AppUser(USERNAME, "encoded-password", Role.USER);
                when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(owner));
        }

        @Test
        void createTask_persistsWithOwnerAndPublishesCreatedEvent()
        {
                TaskRequest request = new TaskRequest("Write tests", "JUnit + Mockito", TaskStatus.TODO);
                when(taskRepository.save(any(Task.class))).thenAnswer(invocation ->
                {
                        Task task = invocation.getArgument(0);
                        task.setId(1L);
                        return task;
                });

                TaskResponse response = taskService.createTask(request, USERNAME);

                assertThat(response.id()).isEqualTo(1L);
                assertThat(response.title()).isEqualTo("Write tests");
                assertThat(response.status()).isEqualTo(TaskStatus.TODO);

                ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
                verify(taskRepository).save(captor.capture());
                assertThat(captor.getValue().getOwner()).isSameAs(owner);

                verify(eventPublisher, times(1)).publishEvent(any(TaskCreatedEvent.class));
                verify(eventPublisher, never()).publishEvent(any(TaskCompletedEvent.class));
        }

        @Test
        void createTask_withDoneStatus_publishesCompletedEvent()
        {
                TaskRequest request = new TaskRequest("Finish", null, TaskStatus.DONE);
                when(taskRepository.save(any(Task.class))).thenAnswer(invocation ->
                {
                        Task task = invocation.getArgument(0);
                        task.setId(2L);
                        return task;
                });

                TaskResponse response = taskService.createTask(request, USERNAME);

                assertThat(response.completedAt()).isNotNull();
                verify(eventPublisher).publishEvent(any(TaskCreatedEvent.class));
                verify(eventPublisher).publishEvent(any(TaskCompletedEvent.class));
        }

        @Test
        void getTaskById_owned_returnsTask()
        {
                Task task = sampleTask(5L, "Existing", TaskStatus.IN_PROGRESS);
                when(taskRepository.findByIdAndOwner(5L, owner)).thenReturn(Optional.of(task));

                TaskResponse response = taskService.getTaskById(5L, USERNAME);

                assertThat(response.id()).isEqualTo(5L);
                assertThat(response.title()).isEqualTo("Existing");
        }

        @Test
        void getTaskById_notOwnedOrMissing_throwsNotFound()
        {
                when(taskRepository.findByIdAndOwner(99L, owner)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> taskService.getTaskById(99L, USERNAME))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("99");
        }

        @Test
        void getAllTasks_returnsOnlyOwnerTasks()
        {
                when(taskRepository.findByOwner(owner)).thenReturn(List.of(
                                sampleTask(1L, "A", TaskStatus.TODO),
                                sampleTask(2L, "B", TaskStatus.DONE)));

                List<TaskResponse> result = taskService.getAllTasks(USERNAME);

                assertThat(result).hasSize(2);
                assertThat(result).extracting(TaskResponse::title).containsExactly("A", "B");
                verify(taskRepository).findByOwner(owner);
        }

        @Test
        void updateTask_transitionToDone_publishesCompletedEventOnce()
        {
                Task existing = sampleTask(7L, "Old", TaskStatus.TODO);
                when(taskRepository.findByIdAndOwner(7L, owner)).thenReturn(Optional.of(existing));
                when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

                TaskRequest request = new TaskRequest("New title", "Updated", TaskStatus.DONE);
                TaskResponse response = taskService.updateTask(7L, request, USERNAME);

                assertThat(response.title()).isEqualTo("New title");
                assertThat(response.status()).isEqualTo(TaskStatus.DONE);
                assertThat(response.completedAt()).isNotNull();

                ArgumentCaptor<TaskCompletedEvent> captor = ArgumentCaptor.forClass(TaskCompletedEvent.class);
                verify(eventPublisher, times(1)).publishEvent(captor.capture());
                assertThat(captor.getValue().taskId()).isEqualTo(7L);
        }

        @Test
        void deleteTask_owned_deletes()
        {
                Task existing = sampleTask(3L, "ToDelete", TaskStatus.TODO);
                when(taskRepository.findByIdAndOwner(3L, owner)).thenReturn(Optional.of(existing));

                taskService.deleteTask(3L, USERNAME);

                verify(taskRepository).delete(existing);
        }

        @Test
        void deleteTask_notOwnedOrMissing_throwsNotFound()
        {
                when(taskRepository.findByIdAndOwner(404L, owner)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> taskService.deleteTask(404L, USERNAME))
                                .isInstanceOf(ResourceNotFoundException.class);
                verify(taskRepository, never()).delete(any());
        }

        private Task sampleTask(Long id, String title, TaskStatus status)
        {
                Task task = new Task();
                task.setId(id);
                task.setTitle(title);
                task.setStatus(status);
                task.setOwner(owner);
                return task;
        }
}
