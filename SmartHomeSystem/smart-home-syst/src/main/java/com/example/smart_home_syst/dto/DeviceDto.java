package com.example.smart_home_syst.dto;

import com.example.smart_home_syst.enumerator.DeviceType;

public record DeviceDto(
    String title,
    DeviceType type,
    Double power,
    Boolean active,
    Long modeId,
    Long roomId) {

}