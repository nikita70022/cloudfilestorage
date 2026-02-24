package com.gigabiba.cloudfilestorage.config.security.authentication.jsonAuthentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigabiba.cloudfilestorage.web.dto.RequestUserDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

public class JsonAuthFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;
    private final AuthenticationManager authenticationManager;


    public JsonAuthFilter(ObjectMapper objectMapper, AuthenticationManager authenticationManager) {
        setFilterProcessesUrl("/api/auth/sign-in");
        this.objectMapper = objectMapper;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String contentType = request.getContentType();
        if (contentType == null || !contentType.contains("application/json")) {
            throw new AuthenticationServiceException("Content-Type must be application/json");
        }

        try {
            RequestUserDto loginResuest = objectMapper.readValue(request.getInputStream(), RequestUserDto.class);

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginResuest.getUsername(),loginResuest.getPassword());

            setDetails(request, token);
            return this.authenticationManager.authenticate(token);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Invalid JSON", e);
        }
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
       return "/api/auth/sign-in".equals(request.getServletPath()) && "POST".equals(request.getMethod());
    }


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = Map.of(
                "message", failed.getMessage()
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }


}


