package com.example.simpleMessenger.mapper;

import com.example.simpleMessenger.dto.*;
import com.example.simpleMessenger.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto userDto);

    User toEntity(UserRegisterDto registerDto);
    UserRegisterDto toRegisterDto(User user);

    UserResponseDto toUserResponseDto(User user);

    UserProfileDto toProfileDto(User user);
    UserListDto toUserListDto(User user);


}
