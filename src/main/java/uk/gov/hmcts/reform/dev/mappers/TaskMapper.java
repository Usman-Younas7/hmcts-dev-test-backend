package uk.gov.hmcts.reform.dev.mappers;

import uk.gov.hmcts.reform.dev.dtos.TaskResponse;
import uk.gov.hmcts.reform.dev.models.Task;

public final class TaskMapper {

    private TaskMapper() {
        // Utility class
    }

    public static TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
            .id(task.getId())
            .title(task.getTitle())
            .description(task.getDescription())
            .status(task.getStatus().name())
            .dueDateTime(task.getDueDateTime())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .build();
    }
}
