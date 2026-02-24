package com.gigabiba.cloudfilestorage.web.controllers;
import com.gigabiba.cloudfilestorage.services.*;
import com.gigabiba.cloudfilestorage.services.minioService.MinioBucketService;
import com.gigabiba.cloudfilestorage.web.dto.*;
import com.gigabiba.cloudfilestorage.web.dto.openApi.authApiDoc;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import org.modelmapper.ModelMapper;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.context.*;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController implements authApiDoc {
    private final UserService userService;
    private final AuthService authService;
    private final MinioBucketService minioBucketService;
    private final ModelMapper modelMapper;

    @Autowired
    public AuthController(AuthService authService, UserService userService, MinioBucketService minioBucketService, ModelMapper modelMapper) {
        this.authService = authService;
        this.userService = userService;
        this.minioBucketService = minioBucketService;
        this.modelMapper = modelMapper;
    }

    @PostMapping(
            value = "/sign-up",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> registration(@RequestBody @Valid RequestUserDto requestUserDto,
                                                                BindingResult errors,
                                                                HttpServletRequest request,
                                                                HttpServletResponse response) throws ValidationException {
        if (errors.hasErrors()) {
            Map<String, String> err = new HashMap<>();
            for (FieldError error : errors.getFieldErrors()) {
                err.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity
                    .status(400)
                    .body(err);
        }

        UserDto userDto = modelMapper.map(requestUserDto, UserDto.class);

        if (userService.usernameIsExist(userDto.getUsername())) {
            return ResponseEntity
                    .status(409)
                    .body(Map.of("username exist ", userDto.getUsername()));
        }

        UserDto user = userService.create(userDto);
        minioBucketService.createBucket(user.getUsername());
        authService.login(userDto.getUsername(), userDto.getPassword(), request, response);

        return ResponseEntity
                .status(201)
                .body(Map.of("username", userDto.getUsername()));
    }

    @PostMapping(
            value = "/sign-in",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid RequestUserDto requestUserDto,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response,
                                                             Authentication authentication) throws BadCredentialsException {

        UserDto userDto = modelMapper.map(requestUserDto, UserDto.class);
        authService.login(userDto.getUsername(), userDto.getPassword(), request, response);

        return ResponseEntity
                .status(200)
                .body(Map.of("username", ((UserDetails) authentication.getPrincipal()).getUsername()));
    }

    @GetMapping(
            value = "/me",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> getMe(Authentication authentication) {

        authService.isAuthenticated(authentication);

        return ResponseEntity
                .status(200)
                .body(Map.of("username", authentication.getName()));
    }

}
