package com.example.smart_home_syst.dto;

import com.example.smart_home_syst.enumerator.ModeType;

public record ModeDto(
    String title,
    ModeType type) {
    
}
