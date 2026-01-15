package com.example.smart_home_syst.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.smart_home_syst.dto.ChangePasswordDto;
import com.example.smart_home_syst.dto.LoginRequestDto;
import com.example.smart_home_syst.dto.LoginResponseDto;
import com.example.smart_home_syst.dto.UserLoggedDto;
import com.example.smart_home_syst.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthentificationController {
    private final AuthenticationService authenticationService;

    @Operation(
    summary = "Try to authentificate",
    description = "Write your username and password. Also if you see Error 401 'Unauthorized' - try login again)")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login (
        @CookieValue(name="access-token",required = false) String accessToken,
        @CookieValue(name="refresh-token",required = false) String refreshToken,
        @RequestBody LoginRequestDto request){
            return authenticationService.login(request,accessToken,refreshToken);
    }

    @Operation(
    summary = "Try to refresh Access token",
    description = "Just send this request, access token will be refreshed")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(
    @CookieValue(name = "refresh-token", required = false) String refresh) {
        return authenticationService.refresh(refresh);
    }

    @Operation(
    summary = "Logout",
    description = "Authentificated user will be forgotten")
    @PostMapping("/logout")
    public ResponseEntity<LoginResponseDto> logout(
    @CookieValue(name = "access-token", required = false) String access) {
        return authenticationService.logout(access);
    }

    @Operation(
    summary = "Info about current user",
    description = "Will be sended info about authentificated user")
    @GetMapping("/info")
    public ResponseEntity <UserLoggedDto> info() {
        return ResponseEntity.ok(authenticationService.info());
    }

    @Operation(
    summary = "Change your password",
    description = "Firstly, you must write your current password. Secondly, current and new password can't be equal. Thirdly, both new passwords must be equal!")
    @PatchMapping("/changePassword")
    public ResponseEntity <LoginResponseDto> changePassword(ChangePasswordDto request) {
        return authenticationService.changePassword(request);
    }
}
