package uk.gov.hmcts.reform.dev.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Task response payload")
public class TaskResponse {

    // Response DTO lets us control exactly what leaves the API.
    @Schema(description = "Unique task ID", example = "1")
    private Long id;

    @Schema(description = "Title of the task", example = "Prepare hearing bundle")
    private String title;

    @Schema(description = "Optional description of the task", example = "Upload all final documents")
    private String description;

    @Schema(description = "Current status", example = "PENDING")
    private String status;

    @Schema(description = "Due date and time", example = "2026-12-31T14:30:00")
    private LocalDateTime dueDateTime;

    @Schema(description = "Timestamp of creation", example = "2026-03-10T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of last update", example = "2026-03-10T10:15:00")
    private LocalDateTime updatedAt;
}
