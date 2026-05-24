package com.gigabiba.cloudfilestorage.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigabiba.cloudfilestorage.dto.UserRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public JsonAuthFilter(ObjectMapper objectMapper, AuthenticationManager authenticationManager) {
        setFilterProcessesUrl("/api/auth/sign-in");
        this.objectMapper = objectMapper;
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        if (request.getContentType() == null || !request.getContentType().contains("application/json")) {
            throw new AuthenticationServiceException("Content-Type must be application/json");
        }
        try {
            UserRequestDto loginResuest = objectMapper.readValue(request.getInputStream(), UserRequestDto.class);
            UsernamePasswordAuthenticationToken token =
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            loginResuest.username(),
                            loginResuest.password());
            setDetails(request, token);
            return getAuthenticationManager().authenticate(token);

        } catch (IOException e) {
            throw new AuthenticationServiceException("Invalid JSON", e);
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> body = Map.of("message", failed.getMessage());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}


