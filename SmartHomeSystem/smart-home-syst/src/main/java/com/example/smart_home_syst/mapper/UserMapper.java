package com.example.smart_home_syst.mapper;

import java.util.stream.Collectors;

import com.example.smart_home_syst.dto.UserDto;
import com.example.smart_home_syst.dto.UserLoggedDto;
import com.example.smart_home_syst.model.Permission;
import com.example.smart_home_syst.model.User;

public class UserMapper { // Преобразует User в UserDto
    public static UserDto userToUserDto (User user) {
        return new UserDto(user.getId(),
        user.getUsername(),
        user.getPassword(),
        user.getRole().getAuthority(),
        user.getRole().getPermissions().stream().map(Permission::getAuthority).collect(Collectors.toSet()));
    }

    public static UserLoggedDto userToUserLoggedDto (User user) {
        return new UserLoggedDto(user.getUsername(),
        user.getRole().getAuthority(),
        user.getRole().getPermissions().stream().map(Permission::getAuthority).collect(Collectors.toSet()));
    }
}