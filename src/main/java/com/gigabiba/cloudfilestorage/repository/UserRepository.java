package com.gigabiba.cloudfilestorage.repository;

import com.gigabiba.cloudfilestorage.models.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsernameIgnoreCase(String login);
}
