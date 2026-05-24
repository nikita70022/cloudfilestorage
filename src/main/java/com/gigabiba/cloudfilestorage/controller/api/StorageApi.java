package com.gigabiba.cloudfilestorage.controller.api;

import com.gigabiba.cloudfilestorage.security.service.UserDetailsImpl;
import com.gigabiba.cloudfilestorage.storage.dto.ObjectResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Tag(name = "storage controller", description = "manage objects (files, folders) in S3 storage")
public interface StorageApi {


    @Operation(summary = "info about file", description = "getting information about file")
    @ApiResponse(responseCode = "200", description = "metadata is received", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ObjectResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "invalid or missing path")
    @ApiResponse(responseCode = "401", description = "unauthorized user")
    @ApiResponse(responseCode = "404", description = "resource not found")
    @ApiResponse(responseCode = "500", description = "unknown error")
    ResponseEntity<ObjectResponseDto> getFileInfo(
            @Parameter(description = "path to resource", example = "documents/photo.jpeg")
            @RequestParam String path,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user);


    @Operation(summary = "download object", description = "allows you to download the file/folder folder and its contents on the path, " +
            "max size of object 2048MB")
    @ApiResponse(responseCode = "200",
            description = "the file has been successfully received",
            content = @Content(
                    mediaType = "application/octet-stream",
                    schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "400", description = "invalid or missing path")
    @ApiResponse(responseCode = "401", description = "unauthorized user")
    @ApiResponse(responseCode = "404", description = "resource not found")
    @ApiResponse(responseCode = "500", description = "unknown error")
    ResponseEntity<StreamingResponseBody> downloadObject(
            @Parameter(description = "full path to storage", example = "documents/doc.txt")
            @RequestParam String path,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user);


    @Operation(summary = "upload objects", description = "upload one or more files to the directory" +
            "and create path to the file through the /, max size of object 2048MB")
    @ApiResponse(responseCode = "201", description = "files have been successfully downloaded",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ObjectResponseDto.class))))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(type = "object", requiredProperties = {"object", "path"}),
                    encoding = @io.swagger.v3.oas.annotations.media.Encoding(name = "object", contentType = "application/octet-stream")
            )
    )
    @ApiResponse(responseCode = "400", description = "invalid body")
    @ApiResponse(responseCode = "401", description = "unauthorized user")
    @ApiResponse(responseCode = "409", description = "resource already exist")
    @ApiResponse(responseCode = "500", description = "unknown error")
    ResponseEntity<List<ObjectResponseDto>> uploadObjects(
            @Parameter(description = "target path", example = "documents/photos")
            @RequestParam String path,
            @Parameter(description = "array for upload")
            List<MultipartFile> files,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user);


    @Operation(summary = "search object", description = "looking for files on a name fragment")
    @ApiResponse(responseCode = "200", description = "successful", content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = ObjectResponseDto.class))))
    @ApiResponse(responseCode = "400", description = "invalid body or not exist path")
    @ApiResponse(responseCode = "401", description = "unauthorized user")
    @ApiResponse(responseCode = "500", description = "unknown error")
    ResponseEntity<List<ObjectResponseDto>> searchObject(
            @Parameter(description = "part of the filename or directory", example = "photo12")
            @RequestParam String query,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user);


    @Operation(summary = "move/rename object", description = "replacement or rename the resource")
    @ApiResponse(responseCode = "200", description = "successful", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ObjectResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "invalid body or path not exist")
    @ApiResponse(responseCode = "401", description = "unauthorized user")
    @ApiResponse(responseCode = "404", description = "resource not found")
    @ApiResponse(responseCode = "409", description = "resource on the path 'to' already exist")
    @ApiResponse(responseCode = "500", description = "unknown error")
    ResponseEntity<ObjectResponseDto> moveObject(
            @Parameter(description = "from", example = "temp/file.txt")
            String from,
            @Parameter(description = "to", example = "documents/file.txt")
            String to,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user);


    @Operation(summary = "get directory info", description = "returns the list of all files and subfolders along the specified user path")
    @ApiResponse(responseCode = "200", description = "the list of resources has been successfully received",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ObjectResponseDto.class))))
    @ApiResponse(responseCode = "400", description = "invalid body or not exist path")
    @ApiResponse(responseCode = "401", description = "unauthorized user")
    @ApiResponse(responseCode = "500", description = "unknown error")
    ResponseEntity<List<ObjectResponseDto>> getDirectoryInfo(
            @Parameter(description = "path to directory (for example, 'documents/work')", example = "photos")
            @RequestParam String path,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user);


    @Operation(summary = "create empty directory", description = "creates a new directory on the path")
    @ApiResponse(responseCode = "201", description = "folder has been successfully created",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ObjectResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "invalid body or not exist path")
    @ApiResponse(responseCode = "401", description = "unauthorized user")
    @ApiResponse(responseCode = "500", description = "unknown error")
    ResponseEntity<ObjectResponseDto> createDirectory(
            @Parameter(description = "path to new directory(for example, 'documents/family')", example = "photos/")
            @RequestParam String path,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user);


    @Operation(summary = "delete resource", description = "removal file or directory from storage")
    @ApiResponse(responseCode = "204", description = "no content",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ObjectResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "invalid or missing path")
    @ApiResponse(responseCode = "401", description = "unauthorized user")
    @ApiResponse(responseCode = "404", description = "resource not found")
    @ApiResponse(responseCode = "500", description = "unknown error")
    ResponseEntity<ObjectResponseDto> deleteObject(
            @Parameter(description = "path to new directory(for example, 'documents/family')", example = "photos/")
            @RequestParam String path,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user);
}