package uk.gov.hmcts.reform.dev.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusRequest {

    // Small dedicated DTO keeps the status-only endpoint explicit.
    @Schema(
        description = "New task status",
        example = "COMPLETED",
        allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED"}
    )
    @NotBlank(message = "Status is required")
    @Pattern(
        regexp = "PENDING|IN_PROGRESS|COMPLETED",
        message = "Status must be one of: PENDING, IN_PROGRESS, COMPLETED"
    )
    private String status;
}
