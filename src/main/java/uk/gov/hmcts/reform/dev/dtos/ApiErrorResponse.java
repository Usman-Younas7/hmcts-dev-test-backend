package uk.gov.hmcts.reform.dev.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Standard API error response")
public class ApiErrorResponse {

    // The frontend uses one error shape for both validation failures and domain errors.
    @Schema(description = "Timestamp when the error occurred", example = "2026-03-10T14:30:00")
    private final String timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private final int status;

    @Schema(description = "Human-readable error summary", example = "Validation failed")
    private final String error;

    @Builder.Default
    @Schema(description = "Field-level validation errors. Empty for non-validation errors.")
    private final Map<String, String> fieldErrors = Map.of();
}
