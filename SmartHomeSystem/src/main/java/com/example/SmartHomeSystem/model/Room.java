package com.example.SmartHomeSystem.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data

@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private User manager;
    @OneToMany()
    private List<Device> devices;
    @NotBlank
    @Size(min=2, max=150, message="Неправильное наименование устройства. Наименование должно содержать не менее 2 и не более 150 символов")
    private String location;
    @NotBlank
    private Mode mode;
}
