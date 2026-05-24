package com.gigabiba.cloudfilestorage.service;

import com.gigabiba.cloudfilestorage.dto.UserDto;
import com.gigabiba.cloudfilestorage.entity.User;
import com.gigabiba.cloudfilestorage.exception.storage.ResourceExistsException;
import com.gigabiba.cloudfilestorage.mapper.UserMapper;
import com.gigabiba.cloudfilestorage.repository.UserRepository;
import com.gigabiba.cloudfilestorage.security.service.Role;
import com.gigabiba.cloudfilestorage.storage.service.UserDirectoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDirectoryService minioUserDirectoryServiceImpl;


    @Transactional
    public UserDto create(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.USER);

        User savedUser = null;
        try {
            savedUser = userRepository.save(user);
            minioUserDirectoryServiceImpl.createUserDirectory(savedUser.getId());
        } catch (DataIntegrityViolationException e) {
            log.error("User with such login {} already exists", username);
            throw new ResourceExistsException(e.getMessage());
        }

        return userMapper.toUserDto(savedUser);
    }
}