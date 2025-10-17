package com.example.SmartHomeSystem.model;

import com.example.SmartHomeSystem.enums.DeviceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data

@Entity
@Table(name="device")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @NotBlank
    @Size(min=2, max=150, message="Неправильное наименование устройства. Наименование должно содержать не менее 2 и не более 150 символов")
    @Column(nullable = false, length = 100)
    private String title;
    @Column(nullable = false, length = 100)
    private Room room;
    @Column(nullable = false, length = 100)
    private DeviceType type;
    @Column(nullable = false, length = 100)
    private Double power;
    @Column(nullable = false, length = 100)
    private boolean isActive;
}
