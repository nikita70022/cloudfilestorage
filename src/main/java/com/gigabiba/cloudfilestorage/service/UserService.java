package com.gigabiba.cloudfilestorage.service;

import com.gigabiba.cloudfilestorage.dto.UserDto;

public interface UserService {
    UserDto create(String username, String password);
}
