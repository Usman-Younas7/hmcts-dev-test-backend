package uk.gov.hmcts.reform.dev.exceptions;

// Raised when a status string cannot be mapped onto the enum used by the domain model.
public class InvalidTaskStatusException extends RuntimeException {

    public InvalidTaskStatusException(String status) {
        super("Invalid task status: " + status + ". Must be one of: PENDING, IN_PROGRESS, COMPLETED");
    }
}
