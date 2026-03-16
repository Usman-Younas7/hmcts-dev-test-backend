package uk.gov.hmcts.reform.dev.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dev.dtos.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dtos.TaskResponse;
import uk.gov.hmcts.reform.dev.dtos.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dtos.UpdateTaskStatusRequest;
import uk.gov.hmcts.reform.dev.exceptions.InvalidTaskStatusException;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.mappers.TaskMapper;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;

@Service
@RequiredArgsConstructor
public class TaskService {

    // The service owns normalisation and domain-level checks before data is persisted.
    private final TaskRepository taskRepository;

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Task task = Task.builder()
            .title(request.getTitle().trim())
            .description(request.getDescription() == null ? null : request.getDescription().trim())
            .status(parseStatus(request.getStatus()))
            .dueDateTime(request.getDueDateTime())
            .build();

        Task saved = taskRepository.save(task);
        return TaskMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        return taskRepository.findById(id)
            .map(TaskMapper::toResponse)
            .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks(String status) {
        List<Task> tasks = status == null || status.isBlank()
            ? taskRepository.findAllByOrderByDueDateTimeAsc()
            : taskRepository.findAllByStatusOrderByDueDateTimeAsc(parseStatus(status));

        return tasks
            .stream()
            .map(TaskMapper::toResponse)
            .toList();
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        task.setStatus(parseStatus(request.getStatus()));
        task.setDueDateTime(request.getDueDateTime());

        Task updated = taskRepository.save(task);
        return TaskMapper.toResponse(updated);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long id, UpdateTaskStatusRequest request) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));

        task.setStatus(parseStatus(request.getStatus()));
        Task updated = taskRepository.save(task);
        return TaskMapper.toResponse(updated);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
        taskRepository.delete(task);
    }

    private TaskStatus parseStatus(String status) {
        // Accept simple string input from the API and convert it to the enum used internally.
        if (status == null || status.isBlank()) {
            throw new InvalidTaskStatusException(String.valueOf(status));
        }

        try {
            return TaskStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidTaskStatusException(status);
        }
    }
}
