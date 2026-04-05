package com.gigabiba.cloudfilestorage.controllers;


import com.gigabiba.cloudfilestorage.config.ConfigTest;
import io.minio.*;
import io.minio.messages.Item;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ConfigTest.class)
@ActiveProfiles("test")
public class StorageControllerIntegrationTest {

    @Autowired
    private MinioClient minioClient;

    @LocalServerPort
    int port;

    @BeforeEach
    void setup() throws Exception {
        baseURI = "http://localhost:" + port;

        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket("user-files")
                        .build());
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket("user-files")
                            .build());
        }
    }

    @AfterEach
    void clean() throws Exception {

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket("user-files")
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : results) {
            Item item = result.get();

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("user-files")
                            .object(item.objectName())
                            .build()
            );
        }
    }


    @Test
    @DisplayName("download file successful")
    @WithAnonymousUser
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/insert_data.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @SneakyThrows
    void should_return_200_when_download_file() {
        Response loginResponse =
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
                    .contentType(ContentType.JSON)
                    .cookie("SESSION", notNullValue())
                    .assertThat()
                    .body("username", is("user1")).extract().response();

        String session = loginResponse.getCookie("SESSION");

        File file = new File("src/test/resources/data/minio/1231.jpg");
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                file.getName(),
                "image/jpeg",
                new FileInputStream(file)
        );

        try (InputStream is = multipartFile.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("user-files")
                            .object("user-1-files" + "/" + multipartFile.getOriginalFilename())
                            .stream(is, multipartFile.getSize(), -1)
                            .contentType(multipartFile.getContentType())
                            .build());
        }

        Response response2 =
                given()
                    .log().all()
                    .redirects().follow(false)
                    .cookie("SESSION", session)
                .when()
                    .get("/api/resource/download?path=1231.jpg")
                .then()
                    .statusCode(200)
                    .contentType("application/octet-stream")
                    .assertThat()
                    .extract().response();

        File downloadedFile = new File("downloaded_file.jpg");
        Files.copy(response2.asInputStream(), downloadedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        assertEquals(file.length(), downloadedFile.length());
        downloadedFile.deleteOnExit();
    }

    @Test
    @DisplayName("download directory successful")
    @WithAnonymousUser
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/insert_data.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @SneakyThrows
    void should_return_200_when_download_directory() {
        Response loginResponse =
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
                    .contentType(ContentType.JSON)
                    .cookie("SESSION", notNullValue())
                    .assertThat()
                    .body("username", is("user1")).extract().response();

        String session = loginResponse.getCookie("SESSION");

        minioClient.putObject(PutObjectArgs.builder()
                .bucket("user-files")
                .object("user-1-files" + "/" + "1/")
                .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                .build());

        File folder = new File("src/test/resources/data/minio/");
        String[] files = folder.list();
        int systemFiles = 0;

        assertNotNull(files);
        for (String file : files) {
            File fullFile = new File(folder, file);

            MultipartFile multipartFile;
            try (FileInputStream fis = new FileInputStream(fullFile)) {
                multipartFile = new MockMultipartFile(
                        "file",
                        file,
                        Files.probeContentType(fullFile.toPath()),
                        fis
                );
            }

            if (multipartFile.getOriginalFilename().equals(".DS_Store")
                    || multipartFile.getOriginalFilename().equals("Thumbs.db")
                    || multipartFile.getOriginalFilename().equals(".directory")) {
                systemFiles += 1;
                continue;
            }

            try (InputStream inputStream = multipartFile.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket("user-files")
                                .object("user-1-files" + "/" + "1/" + multipartFile.getOriginalFilename())
                                .stream(inputStream, multipartFile.getSize(), -1)
                                .contentType(multipartFile.getContentType())
                                .build());
            }
        }

        Response getFolderResponse =
                given()
                    .log().all()
                    .redirects().follow(false)
                    .cookie("SESSION", session)
                .when()
                    .get("/api/resource/download?path=1/")
                .then()
                    .statusCode(200)
                    .contentType("application/octet-stream")
                    .assertThat()
                    .extract().response();

        File downloadedFolder = new File("downloaded_folder.zip");
        Files.copy(getFolderResponse.asInputStream(), downloadedFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);

        int fileCount = 0;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(downloadedFolder))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    fileCount++;
                }
            }
        }

        assertTrue(downloadedFolder.length() > 0);
        assertEquals(files.length - systemFiles, fileCount);
        downloadedFolder.deleteOnExit();
    }

    @Test
    @DisplayName("upload files successful")
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/insert_data.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @SneakyThrows
    void should_return_201_when_upload_file() {
        Response loginResponse =
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
                    .contentType(ContentType.JSON)
                    .cookie("SESSION", notNullValue())
                    .assertThat()
                    .body("username", is("user1")).extract().response();

        String session = loginResponse.getCookie("SESSION");

        minioClient.putObject(PutObjectArgs.builder()
                .bucket("user-files")
                .object("user-1-files" + "/" + "1/")
                .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                .build());

        File folder = new File("src/test/resources/data/minio/");

        RequestSpecification request =
                given()
                    .contentType(ContentType.MULTIPART);

        File[] files = folder.listFiles();
        assertNotNull(files);
        for (File file : files) {
            if (file.isFile()) {
                if (file.getName().equals(".DS_Store")
                        || file.getName().equals("Thumbs.db")
                        || file.getName().equals(".directory")) {
                    continue;
                }
                request.multiPart("object", file);
            }
        }

        request.given()
                    .cookie("SESSION", session)
                    .log().all()
                    .redirects().follow(false)
                .when()
                    .post("/api/resource?path=1/")
                .then()
                    .statusCode(201)
                    .contentType(ContentType.JSON)
                    .body("name", hasItems(
                            "1231.jpg",
                            "2354.jpg",
                            "6292.jpg",
                            "8637.jpg",
                            "3/4/5.jpg"
                    ));
    }

    @Test
    @DisplayName("directory created successful")
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/insert_data.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @SneakyThrows
    void should_return_201_when_directory_created() {
        Response loginResponse =
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
                    .contentType(ContentType.JSON)
                    .cookie("SESSION", notNullValue())
                    .body("username", is("user1")).extract().response();

        String session = loginResponse.getCookie("SESSION");

                given()
                    .cookie("SESSION", session)
                    .log().all()
                    .redirects().follow(false)
                .when()
                    .post("/api/directory?path=1/")
                .then()
                    .statusCode(201)
                    .contentType(ContentType.JSON)
                    .body("path", equalTo(""))
                    .body("name", equalTo("1"))
                    .body("type", equalTo("DIRECTORY"));
    }

    @Test
    @DisplayName("file deleted successful")
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/insert_data.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @SneakyThrows
    void should_return_204_when_file_deleted() {
        Response loginResponse =
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
                    .contentType(ContentType.JSON)
                    .cookie("SESSION", notNullValue())
                    .body("username", is("user1")).extract().response();

        String session = loginResponse.getCookie("SESSION");

        File file = new File("src/test/resources/data/minio/1231.jpg");
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                file.getName(),
                "image/jpeg",
                new FileInputStream(file)
        );

        try (InputStream is = multipartFile.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("user-files")
                            .object("user-1-files" + "/" + multipartFile.getOriginalFilename())
                            .stream(is, multipartFile.getSize(), -1)
                            .contentType(multipartFile.getContentType())
                            .build());
        }

                given()
                    .cookie("SESSION", session)
                    .log().all()
                    .redirects().follow(false)
                .when()
                    .delete("/api/resource?path=1231.jpg")
                .then()
                    .statusCode(204)
                    .contentType("application/octet-stream");
    }

    @Test
    @DisplayName("directory deleted successful")
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/insert_data.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @SneakyThrows
    void should_return_204_when_directory_deleted() {

        Response loginResponse =
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
                    .contentType(ContentType.JSON)
                    .cookie("SESSION", notNullValue())
                    .body("username", is("user1")).extract().response();

        String session = loginResponse.getCookie("SESSION");

        minioClient.putObject(PutObjectArgs.builder()
                .bucket("user-files")
                .object("user-1-files" + "/" + "1/")
                .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                .build());

        File folder = new File("src/test/resources/data/minio/");
        String[] files = folder.list();

        assertNotNull(files);
        for (String file : files) {
            File fullFile = new File(folder, file);

            MultipartFile multipartFile;
            try (FileInputStream fis = new FileInputStream(fullFile)) {
                multipartFile = new MockMultipartFile(
                        "file",
                        file,
                        Files.probeContentType(fullFile.toPath()),
                        fis
                );
            }

            if (multipartFile.getOriginalFilename().equals(".DS_Store")
                || multipartFile.getOriginalFilename().equals("Thumbs.db")
                || multipartFile.getOriginalFilename().equals(".directory")) {
                continue;
            }

            try (InputStream inputStream = multipartFile.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket("user-files")
                                .object("user-1-files" + "/" + "1/" + multipartFile.getOriginalFilename())
                                .stream(inputStream, multipartFile.getSize(), -1)
                                .contentType(multipartFile.getContentType())
                                .build());
            }
        }

                given()
                    .cookie("SESSION", session)
                    .log().all()
                    .redirects().follow(false)
                .when()
                    .delete("/api/resource?path=1/")
                .then()
                    .statusCode(204)
                    .contentType("application/octet-stream");
    }

    @Test
    @DisplayName("file moved successful")
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/insert_data.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @SneakyThrows
    void should_return_200_when_file_moved() {
        Response loginResponse =
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
                    .contentType(ContentType.JSON)
                    .cookie("SESSION", notNullValue())
                    .body("username", is("user1")).extract().response();

        String session = loginResponse.getCookie("SESSION");

        minioClient.putObject(PutObjectArgs.builder()
                .bucket("user-files")
                .object("user-1-files" + "/" + "1/")
                .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                .build());

        minioClient.putObject(PutObjectArgs.builder()
                .bucket("user-files")
                .object("user-1-files" + "/" + "2/")
                .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                .build());

        File file = new File("src/test/resources/data/minio/1231.jpg");
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                file.getName(),
                "image/jpeg",
                new FileInputStream(file)
        );
        try (InputStream is = multipartFile.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("user-files")
                            .object("user-1-files" + "/" + "1/" + multipartFile.getOriginalFilename())
                            .stream(is, multipartFile.getSize(), -1)
                            .contentType(multipartFile.getContentType())
                            .build());
        }

                given()
                    .cookie("SESSION", session)
                    .log().all()
                    .redirects().follow(false)
                .when()
                    .get("/api/resource/move?from=1/1231.jpg&to=2/1231.jpg")
                .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("path", equalTo("2/"))
                    .body("name", equalTo("1231.jpg"))
                    .body("type", equalTo("FILE"));;
    }

    @Test
    @DisplayName("directory moved successful")
    @Sql(scripts = "/data/postgres/clear_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/init_db.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @Sql(scripts = "/data/postgres/insert_data.sql",
            executionPhase = BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
    @SneakyThrows
    void should_return_200_when_directory_moved() {
        Response loginResponse =
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
                    .contentType(ContentType.JSON)
                    .cookie("SESSION", notNullValue())
                    .body("username", is("user1")).extract().response();

        String session = loginResponse.getCookie("SESSION");

        minioClient.putObject(PutObjectArgs.builder()
                .bucket("user-files")
                .object("user-1-files" + "/" + "1/")
                .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                .build());

        minioClient.putObject(PutObjectArgs.builder()
                .bucket("user-files")
                .object("user-1-files" + "/" + "2/")
                .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                .build());

        File folder = new File("src/test/resources/data/minio/");
        String[] files = folder.list();

        assertNotNull(files);
        for (String file : files) {
            File fullFile = new File(folder, file);

            MultipartFile multipartFile;
            try (FileInputStream fis = new FileInputStream(fullFile)) {
                multipartFile = new MockMultipartFile(
                        "file",
                        file,
                        Files.probeContentType(fullFile.toPath()),
                        fis
                );
            }

            if (multipartFile.getOriginalFilename().equals(".DS_Store")
                    || multipartFile.getOriginalFilename().equals("Thumbs.db")
                    || multipartFile.getOriginalFilename().equals(".directory")) {
                continue;
            }

            try (InputStream inputStream = multipartFile.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket("user-files")
                                .object("user-1-files" + "/" + "1/" + multipartFile.getOriginalFilename())
                                .stream(inputStream, multipartFile.getSize(), -1)
                                .contentType(multipartFile.getContentType())
                                .build());
            }
        }

                given()
                    .cookie("SESSION", session)
                    .log().all()
                    .redirects().follow(false)
                .when()
                    .get("/api/resource/move?from=1/&to=2/1/")
                .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("path", equalTo("2/"))
                    .body("name", equalTo("1"))
                    .body("type", equalTo("DIRECTORY"));;

    }
}
