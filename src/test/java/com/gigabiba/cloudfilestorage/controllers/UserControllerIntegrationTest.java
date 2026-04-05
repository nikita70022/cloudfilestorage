package com.gigabiba.cloudfilestorage.controllers;

import com.gigabiba.cloudfilestorage.config.ConfigTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ConfigTest.class)
@ActiveProfiles("test")
public class UserControllerIntegrationTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setup() {
        baseURI = "http://localhost:" + port;
    }

    @Test
    @DisplayName("authorized")
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/insert_data.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    void should_return_200_when_authorized() {
        Response response =
                        given()
                            .contentType(ContentType.JSON)
                            .log().all()
                            .redirects().follow(false)
                            .body("""
                            {
                              "username": "user1",
                              "password": "User123!"
                            }
                            """)
                        .when()
                            .post("/api/auth/sign-in")
                        .then()
                            .statusCode(200)
                            .extract()
                            .response();

        String sessionId = response.getCookie("SESSION");

                        given()
                            .cookie("SESSION", sessionId)
                        .when()
                            .get("/api/user/me")
                        .then()
                            .statusCode(200)
                            .contentType(ContentType.JSON)
                            .assertThat()
                            .body("username", is("user1"));
    }
}
