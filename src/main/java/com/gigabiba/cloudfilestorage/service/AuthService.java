package com.gigabiba.cloudfilestorage.service;

import com.gigabiba.cloudfilestorage.dto.UserRequestDto;
import com.gigabiba.cloudfilestorage.dto.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    UserResponseDto registration(UserRequestDto userRequestDto);
    void autologin(UserRequestDto userRequestDto, HttpServletRequest request, HttpServletResponse response);
}