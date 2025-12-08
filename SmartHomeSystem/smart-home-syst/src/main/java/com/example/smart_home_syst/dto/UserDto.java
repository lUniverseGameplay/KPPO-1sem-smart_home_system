package com.example.smart_home_syst.dto;

import java.io.Serializable;
import java.util.Set;

public record UserDto(Long id, 
String username, 
String password, 
String role, 
Set<String> permissions) implements Serializable {
    
}