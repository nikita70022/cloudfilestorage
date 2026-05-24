package com.gigabiba.cloudfilestorage.mapper;

import com.gigabiba.cloudfilestorage.dto.UserDto;
import com.gigabiba.cloudfilestorage.dto.UserResponseDto;
import com.gigabiba.cloudfilestorage.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserDto toUserDto(User user);

    UserResponseDto toUserResponseDtoFromUserDto(UserDto userDto);
}

