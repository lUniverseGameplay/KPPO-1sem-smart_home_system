package com.example.smart_home_syst.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smart_home_syst.model.Token;


public interface TokenRepository extends JpaRepository <Token, Long> {
    Optional <Token> findByValue(String value);
}