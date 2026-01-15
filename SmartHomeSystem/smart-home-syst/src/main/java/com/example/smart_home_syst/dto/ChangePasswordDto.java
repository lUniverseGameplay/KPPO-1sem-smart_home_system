package com.example.smart_home_syst.dto;

public record ChangePasswordDto(
    String oldPassword,
    String newPassword,
    String newPasswordAgain
) {
    
}
