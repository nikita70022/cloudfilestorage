package com.gigabiba.cloudfilestorage.service;

import com.gigabiba.cloudfilestorage.dto.UserDto;
import com.gigabiba.cloudfilestorage.dto.UserRequestDto;
import com.gigabiba.cloudfilestorage.dto.UserResponseDto;
import com.gigabiba.cloudfilestorage.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserService userServiceImpl;
    private final AuthenticationManager manager;
    private final SecurityContextRepository securityContextRepository;

    public UserResponseDto registration(UserRequestDto userRequestDto) {

        UserDto savedUserDto = userServiceImpl.create(userRequestDto.username(), userRequestDto.password());
        return userMapper.toUserResponseDtoFromUserDto(savedUserDto);
    }

    public void autologin(UserRequestDto userRequestDto, HttpServletRequest request, HttpServletResponse response) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userRequestDto.username(), userRequestDto.password());
        Authentication authentication = manager.authenticate(authToken);
        var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);
    }
}
