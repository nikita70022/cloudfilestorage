package com.gigabiba.cloudfilestorage.repository;

import com.gigabiba.cloudfilestorage.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String login);
}
