package com.example.smart_home_syst.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.smart_home_syst.dto.UserDto;
import com.example.smart_home_syst.exception.ResourceNotFoundException;
import com.example.smart_home_syst.mapper.UserMapper;
import com.example.smart_home_syst.model.User;
import com.example.smart_home_syst.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream().map(UserMapper::userToUserDto).toList();
    }

    public UserDto getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with id " + id + "not found"));
        return UserMapper.userToUserDto(user);
    }

    public UserDto getUserDto(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User with username " + username + "not found"));
        return UserMapper.userToUserDto(user);
    }

    public User getUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User with username " + username + "not found"));
        return user;
    }
}
