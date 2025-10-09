package com.example.SmartHomeSystem.model;

import com.example.SmartHomeSystem.enums.DeviceType;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data

@Entity
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Size(min=2, max=150, message="Неправильное наименование устройства. Наименование должно содержать не менее 2 и не более 150 символов")
    private String title;
    @NotBlank
    private Room room;
    @NotBlank
    private DeviceType type;
    @NotBlank
    private Double power;
    @NotBlank
    private boolean isActive;
}
