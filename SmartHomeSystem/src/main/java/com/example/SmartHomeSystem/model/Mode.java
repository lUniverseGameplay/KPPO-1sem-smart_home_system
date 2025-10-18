package com.example.SmartHomeSystem.model;

import java.util.List;

import com.example.SmartHomeSystem.enums.ModeType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data

@Entity
@Table(name="mode")
public class Mode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // @Column(nullable = false, unique = true ,length = 100)
    private ModeType type;
    @OneToMany(mappedBy = "mode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Device> devices;
}
