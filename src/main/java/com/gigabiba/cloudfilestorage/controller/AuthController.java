package com.gigabiba.cloudfilestorage.controller;
import com.gigabiba.cloudfilestorage.dto.UserRequestDto;
import com.gigabiba.cloudfilestorage.dto.UserResponseDto;
import com.gigabiba.cloudfilestorage.service.AuthService;
import com.gigabiba.cloudfilestorage.service.AuthServiceImpl;
import com.gigabiba.cloudfilestorage.openapi.AuthApiDoc;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api")
public class AuthController implements AuthApiDoc {

    private final AuthService authServiceImpl;

    public AuthController(AuthServiceImpl authServiceImpl) {
        this.authServiceImpl = authServiceImpl;
    }

    @PostMapping(value = "/auth/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE,
                                        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> registration(@RequestBody @Valid UserRequestDto userRequestDto,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) throws ValidationException {

        UserResponseDto userResponseDto = authServiceImpl.registration(userRequestDto);
        authServiceImpl.autologin(userRequestDto, request, response);

        return ResponseEntity.status(201).body(Map.of("username", userResponseDto.username()));
    }


    @PostMapping(value = "/auth/sign-in", consumes = MediaType.APPLICATION_JSON_VALUE,
                                        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid UserRequestDto userRequestDto,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     Authentication authentication) throws BadCredentialsException {

        return ResponseEntity.status(200)
                .body(Map.of("username", userRequestDto.username()));
    }
}
