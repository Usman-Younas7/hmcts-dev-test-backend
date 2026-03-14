package uk.gov.hmcts.reform.dev.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class UpdateTaskRequest {

    @Schema(description = "Title of the task", example = "Prepare hearing bundle")
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be 255 characters or fewer")
    private String title;

    @Schema(description = "Optional description", example = "Upload all final documents before the hearing")
    @Size(max = 2000, message = "Description must be 2000 characters or fewer")
    private String description;

    @Schema(
        description = "Task status",
        example = "IN_PROGRESS",
        allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED"}
    )
    @NotBlank(message = "Status is required")
    @Pattern(
        regexp = "PENDING|IN_PROGRESS|COMPLETED",
        message = "Status must be one of: PENDING, IN_PROGRESS, COMPLETED"
    )
    private String status;

    @Schema(description = "Due date and time", example = "2026-12-31T14:30:00")
    @NotNull(message = "Due date and time is required")
    private LocalDateTime dueDateTime;
}
