package com.gigabiba.cloudfilestorage.controllers;

import com.gigabiba.cloudfilestorage.config.ConfigTest;
import io.minio.*;
import io.minio.messages.Item;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ConfigTest.class)
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Autowired
    private MinioClient minio;

    @LocalServerPort
    int port;



    @BeforeEach
    void setup() throws Exception {
        baseURI = "http://localhost:" + port;

        boolean exists = minio.bucketExists(
                BucketExistsArgs.builder()
                        .bucket("user-files")
                        .build());
        if (!exists) {
            minio.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket("user-files")
                            .build());
        }
    }

    @AfterEach
    void clean() throws Exception {

        Iterable<Result<Item>> results = minio.listObjects(
                ListObjectsArgs.builder()
                        .bucket("user-files")
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : results) {
            Item item = result.get();

            minio.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("user-files")
                            .object(item.objectName())
                            .build()
            );
        }
    }


    @Test
    @DisplayName("registration successful")
    @WithAnonymousUser
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    void should_return_201_when_registration_successful() {
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
                    .post("/api/auth/sign-up")
                .then()
                    .statusCode(201)
                    .contentType(ContentType.JSON)
                    .cookie("SESSION", notNullValue())
                    .assertThat()
                    .body("username", is("user1"));
    }

    @Test
    @DisplayName("registration valid exception")
    @WithAnonymousUser
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    void should_return_400_when_invalid_username_or_password_for_registration() {
                given()
                    .log().all()
                    .redirects().follow(false)
                    .contentType(ContentType.JSON)
                    .body("""
                            {
                              "username": "u",
                              "password": "User123!"
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
    @WithAnonymousUser
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/insert_data.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    void should_return_409_when_user_already_exist() {
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
    @WithAnonymousUser
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/insert_data.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    void should_return_200_when_login_successful() {
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
    @DisplayName("login incorrect username or password")
    @WithAnonymousUser
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/insert_data.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    void should_return_401_when_invalid_username_or_password_for_login() {
                given()
                    .log().all()
                    .redirects().follow(false)
                    .contentType(ContentType.JSON)
                    .body("""
                          {
                            "username": "user12",
                            "password": "User1234!"
                          }
                          """)
                .when()
                    .post("/api/auth/sign-in")
                .then()
                    .statusCode(401);
    }

    @Test
    @DisplayName("logout successful")
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/insert_data.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    void should_return_204_when_was_login() {
        Response response =
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
    @WithAnonymousUser
    void should_return_401_when_unauthorized() {
                given()
                    .log().all()
                    .redirects().follow(false)
                .when()
                    .post("/api/auth/sign-out")
                .then()
                    .statusCode(401);
    }
}