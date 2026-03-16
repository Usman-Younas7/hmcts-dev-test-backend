package uk.gov.hmcts.reform.dev.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class RootController {

    // Handy root endpoint so the service responds on "/" as well as the task routes.
    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        return ok("Welcome to test-backend");
    }
}
