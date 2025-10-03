package com.example.SmartHomeSystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SmartHomeSystem.model.User;

@Repository
public interface UserRepository extends 
        JpaRepository<User, Long> {
        List<User> findByTitleStartingWithIgnoreCase(String title);
        List<User> findAllByTitle(String title);
}