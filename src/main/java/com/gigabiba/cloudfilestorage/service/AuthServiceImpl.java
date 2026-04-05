package com.gigabiba.cloudfilestorage.service;

import com.gigabiba.cloudfilestorage.dto.UserDto;
import com.gigabiba.cloudfilestorage.dto.UserRequestDto;
import com.gigabiba.cloudfilestorage.dto.UserResponseDto;
import com.gigabiba.cloudfilestorage.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService{

    private final UserMapper userMapper;
    private final UserService userServiceImpl;
    private final AuthenticationManager manager;
    private final SecurityContextRepository securityContextRepository;

    public AuthServiceImpl(UserMapper userMapper,
                           UserServiceImpl userServiceImpl,
                           AuthenticationManager manager,
                           SecurityContextRepository securityContextRepository) {
        this.userMapper = userMapper;
        this.userServiceImpl = userServiceImpl;
        this.manager = manager;
        this.securityContextRepository = securityContextRepository;
    }

    public UserResponseDto registration(UserRequestDto userRequestDto) {

        UserDto userDto = userMapper.toUserDtoFromRequest(userRequestDto);
        UserDto savedUserDto = userServiceImpl.create(userDto);
        return userMapper.toUserResponseDtoFromUserDto(savedUserDto);
    }

    public void autologin(UserRequestDto userRequestDto, HttpServletRequest request, HttpServletResponse response) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userRequestDto.username(), userRequestDto.password());
        Authentication authentication = manager.authenticate(authToken);
        var securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
        securityContextRepository.saveContext(securityContext, request, response);
    }
}
