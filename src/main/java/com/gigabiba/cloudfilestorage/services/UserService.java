package com.gigabiba.cloudfilestorage.services;

import com.gigabiba.cloudfilestorage.models.*;
import com.gigabiba.cloudfilestorage.models.User;
import com.gigabiba.cloudfilestorage.repository.*;
import com.gigabiba.cloudfilestorage.web.dto.*;
import org.modelmapper.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;


@Service
@Transactional(readOnly = true)
public class UserService {
    private ModelMapper modelMapper;
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;


    @Autowired
    public UserService(ModelMapper modelMapper, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public UserService() {
    }

    @Transactional
    public boolean usernameIsExist(String login) {
        return userRepository.findByUsernameIgnoreCase(login).isPresent();
    }

    @Transactional
    public UserDto create(UserDto userDto) {
        userDto.setRole(Role.USER.name());
        User user = modelMapper.map(userDto, User.class);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(user);
        return modelMapper.map(user, UserDto.class);
    }
}

