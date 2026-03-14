package uk.gov.hmcts.reform.dev;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class TaskApiFunctionalTest {

    @Value("${TEST_URL:http://localhost:4000}")
    private String testUrl;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();
        taskRepository.deleteAll();
    }

    @Test
    void fullTaskLifecycle() {
        Map<String, Object> createRequest = Map.of(
            "title", "Functional task",
            "description", "Created in functional test",
            "status", "PENDING",
            "dueDateTime", LocalDateTime.now().plusDays(10).withSecond(0).withNano(0).toString()
        );

        Long taskId = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post("/tasks")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        Response getResponse = given()
            .when()
            .get("/tasks/{id}", taskId)
            .then()
            .statusCode(200)
            .extract()
            .response();

        assertThat(getResponse.jsonPath().getString("title")).isEqualTo("Functional task");

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("status", "COMPLETED"))
            .when()
            .put("/tasks/{id}/status", taskId)
            .then()
            .statusCode(200)
            .body("status", org.hamcrest.Matchers.equalTo("COMPLETED"));

        given()
            .when()
            .delete("/tasks/{id}", taskId)
            .then()
            .statusCode(204);

        given()
            .when()
            .get("/tasks/{id}", taskId)
            .then()
            .statusCode(404);
    }
}
