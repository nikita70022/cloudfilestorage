package com.gigabiba.cloudfilestorage.controller;

import com.gigabiba.cloudfilestorage.controller.api.AuthApi;
import com.gigabiba.cloudfilestorage.dto.UserRequestDto;
import com.gigabiba.cloudfilestorage.dto.UserResponseDto;
import com.gigabiba.cloudfilestorage.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authServiceImpl;

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
