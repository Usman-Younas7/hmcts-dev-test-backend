package uk.gov.hmcts.reform.dev.models;

// Stored as explicit strings in the database and sent back to the frontend as-is.
public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}
