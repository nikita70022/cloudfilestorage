package com.gigabiba.cloudfilestorage.mapper;

import com.gigabiba.cloudfilestorage.dto.UserResponseDto;
import com.gigabiba.cloudfilestorage.entity.User;
import com.gigabiba.cloudfilestorage.dto.UserRequestDto;
import com.gigabiba.cloudfilestorage.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    User toUser(UserDto userDto);

    UserDto toUserDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    UserDto toUserDtoFromRequest(UserRequestDto request);

    UserResponseDto toUserResponseDtoFromUser(User user);

    UserResponseDto toUserResponseDtoFromUserDto(UserDto userDto);
}

