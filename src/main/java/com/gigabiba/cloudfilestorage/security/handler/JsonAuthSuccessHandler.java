package com.gigabiba.cloudfilestorage.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class JsonAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final SecurityContextRepository repo;
    private final ObjectMapper objectMapper;

    public JsonAuthSuccessHandler(ObjectMapper objectMapper, SecurityContextRepository securityContextRepository) {
        this.objectMapper = objectMapper;
        this.repo = securityContextRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        repo.saveContext(context, request, response);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "username", authentication.getName()
        ));

    }
}


