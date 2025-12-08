package com.example.smart_home_syst.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.smart_home_syst.dto.LoginRequestDto;
import com.example.smart_home_syst.dto.LoginResponseDto;
import com.example.smart_home_syst.service.AuthenticationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthentificationController {
    private final AuthenticationService authenticationService;
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login (
        @CookieValue(name="access-token",required = false) String accessToken,
        @CookieValue(name="refresh-token",required = false) String refreshToken,
        @RequestBody LoginRequestDto request){
            return authenticationService.login(request,accessToken,refreshToken);
    }
}
