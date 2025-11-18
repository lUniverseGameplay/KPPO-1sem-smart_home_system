package com.example.smart_home_syst.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Device {
    private Long id;
    @NotBlank
    private String title;
    //private Room room;
    //private DeviceType type;
    private Double power;
    private boolean isActive;
}
