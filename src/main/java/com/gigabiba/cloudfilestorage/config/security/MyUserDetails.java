package com.gigabiba.cloudfilestorage.config.security;

import com.gigabiba.cloudfilestorage.models.User;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import java.util.*;
import java.util.stream.*;

public class MyUserDetails implements org.springframework.security.core.userdetails.UserDetails {

    private final User user;

    public MyUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(user.getRole().split(", "))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public String getAuthoritiesAsString() {
        return getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getUsername();
    }

    public Long getId() {
        return this.user.getId();
    }

}
