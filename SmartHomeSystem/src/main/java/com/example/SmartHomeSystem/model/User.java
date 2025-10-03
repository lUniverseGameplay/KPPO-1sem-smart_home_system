package com.example.SmartHomeSystem.model;

import java.util.Set;

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
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Size(min=2, max=150, message="Неправильное имя. Имя должно иметь не меньше 2 и не больше 150 символов")
    private String username;
    @NotBlank
    @Size(min=4, max=150, message="Неправильный пароль. Требуется не меньше 4 и не больше 150 символов")
    private String password;
    @NotBlank
    private boolean enabled;
    @NotBlank
    private Set<Role> roles;
}
