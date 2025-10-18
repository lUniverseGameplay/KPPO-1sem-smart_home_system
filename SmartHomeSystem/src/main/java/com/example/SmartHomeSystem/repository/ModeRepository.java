package com.example.SmartHomeSystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SmartHomeSystem.enums.ModeType;
import com.example.SmartHomeSystem.model.Mode;

@Repository
public interface ModeRepository extends 
    JpaRepository<Mode, Long>{
        Mode findByType(ModeType type);
        List<Mode> findAllByType(ModeType type);
}