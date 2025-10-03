package com.example.SmartHomeSystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SmartHomeSystem.model.Role;

@Repository
public interface RoleRepository extends 
        JpaRepository<Role, Long> {
        List<Role> findByTitleStartingWithIgnoreCase(String title);
        List<Role> findAllByTitle(String title);
}