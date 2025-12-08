package com.example.smart_home_syst.dto;

import java.util.Set;

public record UserLoggedDto(String username,
String role,
Set <String> permissions) {

}

