package com.example.smart_home_syst.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.smart_home_syst.dto.LoginRequestDto;
import com.example.smart_home_syst.dto.LoginResponseDto;
import com.example.smart_home_syst.dto.UserLoggedDto;
import com.example.smart_home_syst.jwt.JwtTokenProvider;
import com.example.smart_home_syst.mapper.UserMapper;
import com.example.smart_home_syst.model.Token;
import com.example.smart_home_syst.model.User;
import com.example.smart_home_syst.repository.TokenRepository;
import com.example.smart_home_syst.util.CookieUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;
    private final AuthenticationManager authenticationManager;

    private final UserService userService;

    @Value("${jwt.access.duration.minutes}")
    private long accessDurationMin;
    @Value("${jwt.access.duration.second}")
    private long accessDurationSec;
    @Value("${jwt.refresh.duration.days}")
    private long refreshDurationDate;
    @Value("${jwt.refresh.duration.second}")
    private long refreshDurationSec;

    private void addAccessTokenCookie(HttpHeaders headers, Token token) {
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.createAccessCookie(token.getValue(), accessDurationSec).toString());
    }

    private void addRefreshTokenCookie(HttpHeaders headers, Token token) {
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshCookie(token.getValue(), refreshDurationSec).toString());
    }

    private void revokeAllTokens(User user) {
        Set <Token> tokens = user.getTokens();
        
        tokens.forEach(token -> {if(token.getExpiringDate().isBefore(LocalDateTime.now())) tokenRepository.delete(token);
        else if(!token.isDisabled()) {
            token.setDisabled(true);
            tokenRepository.save(token);
        }});
    }

    public ResponseEntity<LoginResponseDto> login(LoginRequestDto request, String access, String refresh) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            request.username(), request.password()));
        User user = userService.getUser(request.username());

        boolean accessValid = jwtTokenProvider.isValid(access);
        boolean refreshValid = jwtTokenProvider.isValid(refresh);

        HttpHeaders headers = new HttpHeaders();

        revokeAllTokens(user);

        if (!accessValid) {
            Token newAccess = jwtTokenProvider.generatedAccessToken(Map.of("role", user.getRole().getAuthority()),
            accessDurationMin, ChronoUnit.MINUTES, user);

            newAccess.setUser(user);
            addAccessTokenCookie(headers, newAccess);
            tokenRepository.save(newAccess);
        }

        if (!refreshValid || accessValid) {
            Token newRefresh = jwtTokenProvider.generatedRefreshToken(refreshDurationDate, ChronoUnit.MINUTES, user);

            newRefresh.setUser(user);
            addRefreshTokenCookie(headers, newRefresh);
            tokenRepository.save(newRefresh);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(true, user.getRole().getName()));
    }

    public ResponseEntity<LoginResponseDto> refresh(String refreshToken){
        if(!jwtTokenProvider.isValid(refreshToken)){
            throw new RuntimeException("token is in valid");
        }

        User user = userService.getUser(jwtTokenProvider.getUsername(refreshToken));
        Token newAcess = jwtTokenProvider.generatedAccessToken(Map.of(), accessDurationMin, null, user);
         HttpHeaders headers = new HttpHeaders();
         addAccessTokenCookie(headers, newAcess);

        LoginResponseDto loginResponseDto = new LoginResponseDto(true, user.getRole().getName());
        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(true, user.getRole().getName()));
    }

    public ResponseEntity<LoginResponseDto> logout(String access, String refresh){
        SecurityContextHolder.clearContext();
        User user = userService.getUser(jwtTokenProvider.getUsername(access));
        revokeAllTokens(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessCookie().toString());
        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(true, user.getRole().getName()));
    }

    public UserLoggedDto info(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof AnonymousAuthenticationToken) throw new RuntimeException("No user");
        User user = userService.getUser(authentication.getName());
        return UserMapper.userToUserLoggedDto(user);
    }
}
