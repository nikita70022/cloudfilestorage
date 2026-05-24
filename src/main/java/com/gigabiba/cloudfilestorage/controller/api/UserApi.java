package com.gigabiba.cloudfilestorage.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;

@Tag(name = "user controller", description = "management of the user controller")
public interface UserApi {

    @Operation(summary = "authorization verification", description = "checking session")
    @ApiResponse(responseCode = "200", description = "returns username of user",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "401", description = "the session is not exists")
    @ApiResponse(responseCode = "500", description = "unknown error")
    ResponseEntity<Map<String, String>> getMe(
            @Parameter(hidden = true)
            Authentication authentication);
}
