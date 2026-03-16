package uk.gov.hmcts.reform.dev.exceptions;

// Raised when a route references a task id that does not exist in the database.
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("Task not found with id: " + id);
    }
}
