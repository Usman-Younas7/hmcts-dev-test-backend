package uk.gov.hmcts.reform.dev.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void shouldCreateTask() throws Exception {
        Map<String, Object> request = Map.of(
            "title", "Test task",
            "description", "Task description",
            "status", "PENDING",
            "dueDateTime", LocalDateTime.now().plusDays(5).withSecond(0).withNano(0).toString()
        );

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Test task"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturnTasksOrderedByDueDate() throws Exception {
        taskRepository.save(Task.builder()
            .title("Later task")
            .status(TaskStatus.PENDING)
            .dueDateTime(LocalDateTime.now().plusDays(5))
            .build());
        taskRepository.save(Task.builder()
            .title("Sooner task")
            .status(TaskStatus.IN_PROGRESS)
            .dueDateTime(LocalDateTime.now().plusDays(1))
            .build());

        mockMvc.perform(get("/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].title").value("Sooner task"))
            .andExpect(jsonPath("$[1].title").value("Later task"));
    }

    @Test
    void shouldFilterTasksByStatus() throws Exception {
        taskRepository.save(Task.builder()
            .title("Pending task")
            .status(TaskStatus.PENDING)
            .dueDateTime(LocalDateTime.now().plusDays(5))
            .build());
        taskRepository.save(Task.builder()
            .title("Completed task")
            .status(TaskStatus.COMPLETED)
            .dueDateTime(LocalDateTime.now().plusDays(1))
            .build());

        mockMvc.perform(get("/tasks").param("status", "COMPLETED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title").value("Completed task"))
            .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    void shouldReturn400ForInvalidStatusFilter() throws Exception {
        mockMvc.perform(get("/tasks").param("status", "UNKNOWN"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "Invalid task status: UNKNOWN. Must be one of: PENDING, IN_PROGRESS, COMPLETED"
            ))
            .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturn404ForMissingTask() throws Exception {
        mockMvc.perform(get("/tasks/99999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Task not found with id: 99999"))
            .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturn400ForInvalidRequest() throws Exception {
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation failed"))
            .andExpect(jsonPath("$.fieldErrors.title").exists())
            .andExpect(jsonPath("$.fieldErrors.status").exists())
            .andExpect(jsonPath("$.fieldErrors.dueDateTime").exists());
    }

    @Test
    void shouldReturn400ForInvalidStatusOnCreate() throws Exception {
        Map<String, Object> request = Map.of(
            "title", "Test task",
            "description", "Task description",
            "status", "UNKNOWN",
            "dueDateTime", LocalDateTime.now().plusDays(5).withSecond(0).withNano(0).toString()
        );

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation failed"))
            .andExpect(jsonPath("$.fieldErrors.status").value(
                "Status must be one of: PENDING, IN_PROGRESS, COMPLETED"
            ));
    }

    @Test
    void shouldUpdateTaskStatus() throws Exception {
        Task savedTask = taskRepository.save(Task.builder()
            .title("Updatable task")
            .status(TaskStatus.PENDING)
            .dueDateTime(LocalDateTime.now().plusDays(2))
            .build());

        mockMvc.perform(put("/tasks/{id}/status", savedTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status":"COMPLETED"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldUpdateTaskFully() throws Exception {
        Task savedTask = taskRepository.save(Task.builder()
            .title("Original task")
            .description("Original description")
            .status(TaskStatus.PENDING)
            .dueDateTime(LocalDateTime.now().plusDays(2))
            .build());
        String updatedDueDateTime = LocalDateTime.now()
            .minusDays(1)
            .withSecond(0)
            .withNano(0)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        Map<String, Object> request = Map.of(
            "title", "Updated task",
            "description", "Updated description",
            "status", "COMPLETED",
            "dueDateTime", updatedDueDateTime
        );

        mockMvc.perform(put("/tasks/{id}", savedTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated task"))
            .andExpect(jsonPath("$.description").value("Updated description"))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.dueDateTime").value(updatedDueDateTime));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        Task savedTask = taskRepository.save(Task.builder()
            .title("Delete task")
            .status(TaskStatus.PENDING)
            .dueDateTime(LocalDateTime.now().plusDays(3))
            .build());

        mockMvc.perform(delete("/tasks/{id}", savedTask.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/{id}", savedTask.getId()))
            .andExpect(status().isNotFound());
    }
}
