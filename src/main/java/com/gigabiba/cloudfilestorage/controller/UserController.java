package com.gigabiba.cloudfilestorage.controller;

import com.gigabiba.cloudfilestorage.openapi.UserApiDoc;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/user")
public class UserController implements UserApiDoc {

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> getMe(Authentication authentication) {

        return ResponseEntity.status(200).body(Map.of("username", authentication.getName()));
    }
}
