package uk.gov.hmcts.reform.dev.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dev.dtos.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dtos.TaskResponse;
import uk.gov.hmcts.reform.dev.dtos.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dtos.UpdateTaskStatusRequest;
import uk.gov.hmcts.reform.dev.exceptions.InvalidTaskStatusException;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldCreateTaskSuccessfully() {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("  Prepare hearing bundle  ")
            .description("  Upload all final documents  ")
            .status("pending")
            .dueDateTime(LocalDateTime.now().plusDays(2))
            .build();

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            task.setCreatedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            return task;
        });

        TaskResponse response = taskService.createTask(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Prepare hearing bundle");
        assertThat(response.getDescription()).isEqualTo("Upload all final documents");
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldThrowWhenTaskNotFound() {
        when(taskRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(42L))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("42");
    }

    @Test
    void shouldReturnAllTasksOrderedByDueDate() {
        Task firstTask = Task.builder()
            .id(1L)
            .title("First")
            .status(TaskStatus.PENDING)
            .dueDateTime(LocalDateTime.now().plusDays(1))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        Task secondTask = Task.builder()
            .id(2L)
            .title("Second")
            .status(TaskStatus.COMPLETED)
            .dueDateTime(LocalDateTime.now().plusDays(2))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(taskRepository.findAllByOrderByDueDateTimeAsc()).thenReturn(List.of(firstTask, secondTask));

        List<TaskResponse> tasks = taskService.getAllTasks(null);

        assertThat(tasks).extracting(TaskResponse::getTitle).containsExactly("First", "Second");
    }

    @Test
    void shouldReturnTasksFilteredByStatus() {
        Task completedTask = Task.builder()
            .id(3L)
            .title("Completed")
            .status(TaskStatus.COMPLETED)
            .dueDateTime(LocalDateTime.now().plusDays(3))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(taskRepository.findAllByStatusOrderByDueDateTimeAsc(TaskStatus.COMPLETED))
            .thenReturn(List.of(completedTask));

        List<TaskResponse> tasks = taskService.getAllTasks("completed");

        assertThat(tasks).extracting(TaskResponse::getTitle).containsExactly("Completed");
    }

    @Test
    void shouldUpdateTaskStatus() {
        Task task = Task.builder()
            .id(5L)
            .title("Update me")
            .status(TaskStatus.PENDING)
            .dueDateTime(LocalDateTime.now().plusDays(1))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponse response = taskService.updateTaskStatus(5L, new UpdateTaskStatusRequest("completed"));

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

    @Test
    void shouldUpdateTaskFully() {
        LocalDateTime updatedDueDateTime = LocalDateTime.now().minusDays(1).withSecond(0).withNano(0);
        Task task = Task.builder()
            .id(6L)
            .title("Existing title")
            .description("Existing description")
            .status(TaskStatus.PENDING)
            .dueDateTime(LocalDateTime.now().plusDays(2))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        UpdateTaskRequest request = UpdateTaskRequest.builder()
            .title("  Updated title  ")
            .description("  Updated description  ")
            .status("in_progress")
            .dueDateTime(updatedDueDateTime)
            .build();

        when(taskRepository.findById(6L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponse response = taskService.updateTask(6L, request);

        assertThat(response.getTitle()).isEqualTo("Updated title");
        assertThat(response.getDescription()).isEqualTo("Updated description");
        assertThat(response.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(response.getDueDateTime()).isEqualTo(updatedDueDateTime);
        assertThat(task.getTitle()).isEqualTo("Updated title");
        assertThat(task.getDescription()).isEqualTo("Updated description");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getDueDateTime()).isEqualTo(updatedDueDateTime);
    }

    @Test
    void shouldDeleteTask() {
        Task task = Task.builder()
            .id(11L)
            .title("Delete me")
            .status(TaskStatus.PENDING)
            .dueDateTime(LocalDateTime.now().plusDays(1))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(taskRepository.findById(11L)).thenReturn(Optional.of(task));
        taskService.deleteTask(11L);

        verify(taskRepository).delete(task);
    }

    @Test
    void shouldThrowForInvalidStatus() {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Invalid")
            .status("unknown")
            .dueDateTime(LocalDateTime.now().plusDays(1))
            .build();

        assertThatThrownBy(() -> taskService.createTask(request))
            .isInstanceOf(InvalidTaskStatusException.class)
            .hasMessageContaining("unknown");
    }

    @Test
    void shouldThrowForInvalidStatusFilter() {
        assertThatThrownBy(() -> taskService.getAllTasks("unknown"))
            .isInstanceOf(InvalidTaskStatusException.class)
            .hasMessageContaining("unknown");
    }

    @Test
    void shouldThrowWhenDeletingMissingTask() {
        when(taskRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(77L))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("77");
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentTask() {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
            .title("Updated title")
            .status("PENDING")
            .dueDateTime(LocalDateTime.now().plusDays(1))
            .build();

        when(taskRepository.findById(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(88L, request))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("88");
    }
}
