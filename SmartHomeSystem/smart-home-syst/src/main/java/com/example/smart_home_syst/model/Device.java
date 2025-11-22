package com.example.smart_home_syst.model;

import jakarta.persistence.Column;
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
    @Size(min=1, max=150)
    @Column(nullable = false, unique = true, length = 150)
    private String title;

    //private Room room;
    //private DeviceType type;

    //@Column(columnDefinition = "double default 0.0")
    private Double power;
    //@Column(columnDefinition = "boolean default false")
    private boolean isActive;
}
