package com.example.smart_home_syst.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.smart_home_syst.dto.UserDto;
import com.example.smart_home_syst.exception.ResourceNotFoundException;
import com.example.smart_home_syst.mapper.UserMapper;
import com.example.smart_home_syst.model.User;
import com.example.smart_home_syst.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);
    
    public List<UserDto> getUsers() {
        List<UserDto> userList = userRepository.findAll().stream().map(UserMapper::userToUserDto).toList();
        logger.info("Successfully founded {} user", userList.size());
        return userList; // Возврат списка всех пользователей в формате Dto. Stream преобразует список в поток
    }

    public UserDto getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            logger.warn("User with id {} not found", id.toString());
            return new ResourceNotFoundException("User with id " + id + "not found");
        }); // Возврат пользователя с указанным Id, в формате Dto. Также есть обработка исключений
        logger.info("User with id {} successfully found", id.toString());
        return UserMapper.userToUserDto(user);
    }

    public UserDto getUserDto(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> {
            logger.warn("User with username {} not found", username);
            return new ResourceNotFoundException("User with username " + username + "not found");
        }); // Возврат сущности Дто пользователя с указанным именем. Также есть обработка исключений
        logger.info("User with username {} successfully found", username);
        return UserMapper.userToUserDto(user);
    }

    public User getUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> {
            logger.warn("User with username {} not found", username);
            return new ResourceNotFoundException("User with username " + username + "not found");
        }); // Возврат сущности пользователя с указанным именем. Также есть обработка исключений
        logger.info("User with username {} successfully found", username);
        return user;
    }
    
    @Transactional
    public void saveUser(User user) {
        userRepository.save(user);
        logger.info("User with username {} successfully saved", user.getUsername());
    }
}
