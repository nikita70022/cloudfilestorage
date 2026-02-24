package com.gigabiba.cloudfilestorage.web.dto.openApi;

import com.gigabiba.cloudfilestorage.web.dto.RequestUserDto;
import com.gigabiba.cloudfilestorage.web.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "authentication controller", description = "management of the authentication controller")
public interface authApiDoc {

    @Operation(summary = "registration of a new user", description = "creates a new user in the system. the login should be unique")
    @ApiResponse(responseCode = "201", description = "the user has been successfully created and returns username of user",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "400", description = "validation error")
    @ApiResponse(responseCode = "409", description = "the user with this login already exists")
    @ApiResponse(responseCode = "500", description = "unknown error")
    public ResponseEntity<Map<String, String>> registration(@RequestBody @Valid RequestUserDto requestUserDto,
                                                            BindingResult errors,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response);

    @Operation(summary = "login to the system", description = "checking credentials and create a session")
    @ApiResponse(responseCode = "200", description = "successful authorization and returns username of user",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "401", description = "incorrect username or password")
    @ApiResponse(responseCode = "500", description = "unknown error")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody @Valid RequestUserDto requestUserDto,
            HttpServletRequest request,
            HttpServletResponse response,
            @Parameter(hidden = true)
            Authentication authentication);

    @Operation(summary = "authorization verification", description = "checking session")
    @ApiResponse(responseCode = "200", description = "returns username of user",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "401", description = "the session is not exists")
    @ApiResponse(responseCode = "500", description = "unknown error")
    public ResponseEntity<Map<String, String>> getMe(
            @Parameter(hidden = true)
            Authentication authentication);
}
