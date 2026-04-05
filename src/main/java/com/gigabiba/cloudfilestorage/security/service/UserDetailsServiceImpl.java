package com.gigabiba.cloudfilestorage.security.service;

import com.gigabiba.cloudfilestorage.entity.User;
import com.gigabiba.cloudfilestorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(login);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(login + " user not found");
        }
        return new UserDetailsImpl(user.get());
    }
}
