package uk.gov.hmcts.reform.dev.exceptions;

public class InvalidTaskStatusException extends RuntimeException {

    public InvalidTaskStatusException(String status) {
        super("Invalid task status: " + status + ". Must be one of: PENDING, IN_PROGRESS, COMPLETED");
    }
}
