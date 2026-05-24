package com.gigabiba.cloudfilestorage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gigabiba.cloudfilestorage.security.service.Role;
import com.gigabiba.cloudfilestorage.storage.util.validation.S3BucketNameValidation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Long id;

    @S3BucketNameValidation
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private Role role;

}
