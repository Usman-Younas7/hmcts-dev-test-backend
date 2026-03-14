package uk.gov.hmcts.reform.dev.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dev.dtos.ApiErrorResponse;
import uk.gov.hmcts.reform.dev.dtos.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dtos.TaskResponse;
import uk.gov.hmcts.reform.dev.dtos.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dtos.UpdateTaskStatusRequest;
import uk.gov.hmcts.reform.dev.services.TaskService;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;

    @Operation(
        summary = "Create a new task",
        description = "Creates a task with a title, optional description, status, and due date/time."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Task created successfully",
                content = @Content(schema = @Schema(implementation = TaskResponse.class))
                ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error - missing or invalid fields",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                )
        }
    )
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse createdTask = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @Operation(
        summary = "Retrieve all tasks",
        description = "Returns all tasks ordered by due date. Optionally filter by status."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "List of tasks returned successfully",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class)))
                ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid status filter value",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                )
        }
    )
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(
        @Parameter(description = "Filter by task status (PENDING, IN_PROGRESS, COMPLETED)", example = "COMPLETED")
        @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(taskService.getAllTasks(status));
    }

    @Operation(summary = "Retrieve a task by ID")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Task found",
                content = @Content(schema = @Schema(implementation = TaskResponse.class))
                ),
            @ApiResponse(
                responseCode = "404",
                description = "Task not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                )
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
        @Parameter(description = "Task ID", example = "1") @PathVariable Long id
    ) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @Operation(
        summary = "Update all task fields",
        description = "Updates the title, optional description, status, and due date/time of an existing task."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Task updated successfully",
                content = @Content(schema = @Schema(implementation = TaskResponse.class))
                ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error - missing or invalid fields",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                ),
            @ApiResponse(
                responseCode = "404",
                description = "Task not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                )
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
        @Parameter(description = "Task ID", example = "1") @PathVariable Long id,
        @Valid @RequestBody UpdateTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @Operation(
        summary = "Update the status of a task",
        description = "Changes the status of an existing task to PENDING, IN_PROGRESS, or COMPLETED."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Status updated successfully",
                content = @Content(schema = @Schema(implementation = TaskResponse.class))
                ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid status value",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                ),
            @ApiResponse(
                responseCode = "404",
                description = "Task not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                )
        }
    )
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
        @Parameter(description = "Task ID", example = "1") @PathVariable Long id,
        @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, request));
    }

    @Operation(summary = "Delete a task by ID")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(
                responseCode = "404",
                description = "Task not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                )
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
        @Parameter(description = "Task ID", example = "1") @PathVariable Long id
    ) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
