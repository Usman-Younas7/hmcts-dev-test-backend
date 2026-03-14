package uk.gov.hmcts.reform.dev.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.dev.dtos.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dtos.TaskResponse;
import uk.gov.hmcts.reform.dev.dtos.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dtos.UpdateTaskStatusRequest;
import uk.gov.hmcts.reform.dev.exceptions.GlobalExceptionHandler;
import uk.gov.hmcts.reform.dev.exceptions.InvalidTaskStatusException;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.services.TaskService;

@WebMvcTest(TaskController.class)
@Import(GlobalExceptionHandler.class)
class TaskControllerTest {

    private static final LocalDateTime DUE_DATE_TIME = LocalDateTime.of(2026, 12, 31, 14, 30);
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 3, 10, 9, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 3, 10, 10, 15);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateTaskAndReturn201() throws Exception {
        TaskResponse taskResponse = buildTaskResponse(1L, "PENDING");

        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(taskResponse);

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "title", "Prepare hearing bundle",
                    "description", "Upload all final documents",
                    "status", "PENDING",
                    "dueDateTime", DUE_DATE_TIME.toString()
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Prepare hearing bundle"))
            .andExpect(jsonPath("$.status").value("PENDING"));

        verify(taskService).createTask(any(CreateTaskRequest.class));
    }

    @Test
    void shouldReturn400WhenCreateRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation failed"))
            .andExpect(jsonPath("$.fieldErrors.title").value("Title is required"))
            .andExpect(jsonPath("$.fieldErrors.status").value("Status is required"))
            .andExpect(jsonPath("$.fieldErrors.dueDateTime").value("Due date and time is required"));
    }

    @Test
    void shouldReturnAllTasks() throws Exception {
        when(taskService.getAllTasks(null)).thenReturn(List.of(buildTaskResponse(1L, "PENDING")));

        mockMvc.perform(get("/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(taskService).getAllTasks(null);
    }

    @Test
    void shouldReturnTasksFilteredByStatus() throws Exception {
        when(taskService.getAllTasks("COMPLETED")).thenReturn(List.of(buildTaskResponse(2L, "COMPLETED")));

        mockMvc.perform(get("/tasks").param("status", "COMPLETED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].status").value("COMPLETED"));

        verify(taskService).getAllTasks("COMPLETED");
    }

    @Test
    void shouldReturn400ForInvalidStatusFilter() throws Exception {
        when(taskService.getAllTasks("BOGUS")).thenThrow(new InvalidTaskStatusException("BOGUS"));

        mockMvc.perform(get("/tasks").param("status", "BOGUS"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "Invalid task status: BOGUS. Must be one of: PENDING, IN_PROGRESS, COMPLETED"
            ))
            .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnTaskById() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(buildTaskResponse(1L, "PENDING"));

        mockMvc.perform(get("/tasks/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Prepare hearing bundle"));

        verify(taskService).getTaskById(1L);
    }

    @Test
    void shouldReturn404WhenTaskNotFound() throws Exception {
        when(taskService.getTaskById(999L)).thenThrow(new TaskNotFoundException(999L));

        mockMvc.perform(get("/tasks/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Task not found with id: 999"))
            .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldUpdateTaskAndReturn200() throws Exception {
        TaskResponse taskResponse = buildTaskResponse(1L, "IN_PROGRESS");

        when(taskService.updateTask(any(Long.class), any(UpdateTaskRequest.class))).thenReturn(taskResponse);

        mockMvc.perform(put("/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "title", "Prepare hearing bundle",
                    "description", "Upload all final documents",
                    "status", "IN_PROGRESS",
                    "dueDateTime", DUE_DATE_TIME.minusDays(1).toString()
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(taskService).updateTask(any(Long.class), any(UpdateTaskRequest.class));
    }

    @Test
    void shouldReturn400WhenUpdateRequestIsInvalid() throws Exception {
        mockMvc.perform(put("/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation failed"))
            .andExpect(jsonPath("$.fieldErrors.title").value("Title is required"))
            .andExpect(jsonPath("$.fieldErrors.status").value("Status is required"))
            .andExpect(jsonPath("$.fieldErrors.dueDateTime").value("Due date and time is required"));
    }

    @Test
    void shouldUpdateTaskStatusAndReturn200() throws Exception {
        when(taskService.updateTaskStatus(any(Long.class), any(UpdateTaskStatusRequest.class)))
            .thenReturn(buildTaskResponse(1L, "COMPLETED"));

        mockMvc.perform(put("/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "COMPLETED"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(taskService).updateTaskStatus(any(Long.class), any(UpdateTaskStatusRequest.class));
    }

    @Test
    void shouldReturn400WhenUpdateStatusBodyIsInvalid() throws Exception {
        mockMvc.perform(put("/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation failed"))
            .andExpect(jsonPath("$.fieldErrors.status").value("Status is required"));
    }

    @Test
    void shouldDeleteTaskAndReturn204() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/tasks/1"))
            .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentTask() throws Exception {
        doThrow(new TaskNotFoundException(999L)).when(taskService).deleteTask(999L);

        mockMvc.perform(delete("/tasks/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Task not found with id: 999"))
            .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    private static TaskResponse buildTaskResponse(Long id, String status) {
        return TaskResponse.builder()
            .id(id)
            .title("Prepare hearing bundle")
            .description("Upload all final documents")
            .status(status)
            .dueDateTime(DUE_DATE_TIME)
            .createdAt(CREATED_AT)
            .updatedAt(UPDATED_AT)
            .build();
    }
}
