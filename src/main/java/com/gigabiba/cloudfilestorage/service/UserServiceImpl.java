package com.gigabiba.cloudfilestorage.service;

import com.gigabiba.cloudfilestorage.dto.UserDto;
import com.gigabiba.cloudfilestorage.exception.storage.ResourceExistsException;
import com.gigabiba.cloudfilestorage.security.service.Role;
import com.gigabiba.cloudfilestorage.mapper.UserMapper;
import com.gigabiba.cloudfilestorage.entity.User;
import com.gigabiba.cloudfilestorage.repository.UserRepository;
import com.gigabiba.cloudfilestorage.storage.minio.client.MinioUserDirectoryServiceImpl;
import com.gigabiba.cloudfilestorage.storage.service.UserDirectoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.Optional;


@Service
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDirectoryService minioUserDirectoryServiceImpl;


    public UserServiceImpl(MinioUserDirectoryServiceImpl minioUserDirectoryServiceImpl,
                           PasswordEncoder passwordEncoder,
                           UserRepository userRepository,
                           UserMapper userMapper) {
        this.minioUserDirectoryServiceImpl = minioUserDirectoryServiceImpl;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }


    @Transactional
    public UserDto create(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(Role.USER);

        User savedUser = null;
        try {
            savedUser = userRepository.save(user);
            minioUserDirectoryServiceImpl.createUserDirectory(Optional.of(savedUser).get().getId());
        } catch (DataIntegrityViolationException e) {
            log.error("User with such login {} already exists", userDto.getUsername());
            throw new ResourceExistsException(e.getMessage());
        }

        return userMapper.toUserDto(savedUser);
    }
}

