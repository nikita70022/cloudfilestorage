package com.gigabiba.cloudfilestorage.controllers;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.server.*;

import org.springframework.security.test.context.support.*;
import org.springframework.test.context.*;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void set_up() {
        baseURI = "http://localhost:" + port;
    }

    @Test
    @DisplayName("registration successful")
    @Sql("/data/clear_db.sql")
    @WithAnonymousUser
    void should_return_201_from_registration_endpoint() {
            given()
                .contentType(ContentType.JSON)
                .log().all()
                .redirects().follow(false)
                .body("""
                        {
                          "username": "test123",
                          "password": "Test123!"
                        }
                        """)
            .when()
                .post("/api/auth/sign-up")
            .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .cookie("SESSION", notNullValue())
                .assertThat()
                .body("username", is("test123"));
    }

    @Test
    @DisplayName("registration valid exception")
    @Sql("/data/clear_db.sql")
    @WithAnonymousUser
    void should_return_400_from_registration_endpoint() {
            given()
                .log().all()
                .redirects().follow(false)
                .contentType(ContentType.JSON)
                .body("""
                          {
                            "username": "t",
                            "password": "Test123!"
                          }
                        """)
            .when()
                .contentType(ContentType.JSON)
                .post("/api/auth/sign-up")
            .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("registration conflict")
    @Sql("/data/clear_db.sql")
    @Sql("/data/init_data.sql")
    @WithAnonymousUser
    void should_return_409_from_registration_endpoint() {
            given()
                .log().all()
                .redirects().follow(false)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "user1",
                          "password": "User123!"
                        }
                        """)
            .when()
                .post("/api/auth/sign-up")
            .then()
                .statusCode(409);
    }

    @Test
    @DisplayName("login successful")
    @Sql("/data/clear_db.sql")
    @Sql("/data/init_data.sql")
    @WithAnonymousUser
    void should_return_200_from_login_endpoint() {
            given()
                .log().all()
                .redirects().follow(false)
                .contentType(ContentType.JSON)
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
                .assertThat()
                .body("username", is("user1"));
    }

    @Test
    @DisplayName("login incorrect username of password")
    @Sql("/data/clear_db.sql")
    @WithAnonymousUser
    void should_return_401_from_login_endpoint() {
            given()
                .log().all()
                .redirects().follow(false)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "user1",
                          "password": "User123!"
                        }
                        """)
            .when()
                .post("/api/auth/sign-in")
            .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("logout successful")
    @Sql("/data/clear_db.sql")
    @Sql("/data/init_data.sql")
    void should_return_204_from_logout_endpoint() {
            Response response = given()
                .log().all()
                .redirects().follow(false)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "user1",
                          "password": "User123!"
                        }
                        """)
            .when()
                .post("/api/auth/sign-in")
            .then()
                .statusCode(200).extract().response();

            String session = response.getCookie("SESSION");

            given()
                .log().all()
                .redirects().follow(false)
                .cookie("SESSION", session)
            .when()
                .post("/api/auth/sign-out")
            .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("logout unauthorized user")
    @Sql("/data/clear_db.sql")
    @WithAnonymousUser
    void should_return_401_from_logout_endpoint() {
            given()
                .log().all()
                .redirects().follow(false)
            .when()
                .post("/api/auth/sign-out")
            .then()
                .statusCode(401);
    }
}