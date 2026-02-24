package com.gigabiba.cloudfilestorage.models;

import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;

public enum Role implements GrantedAuthority {
    ADMIN, USER;

    @Override
    public String getAuthority() {
        return name();
    }

    public SimpleGrantedAuthority toAuthority() {
        return new SimpleGrantedAuthority("ROLE_" + this.name());
    }
}
