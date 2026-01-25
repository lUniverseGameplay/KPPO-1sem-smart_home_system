package com.example.smart_home_syst.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.smart_home_syst.dto.ChangePasswordDto;
import com.example.smart_home_syst.dto.ChangeTgChatIdDto;
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
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    private final TgBotService botService;
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
        logger.debug("Adding Access token cookie");
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.createAccessCookie(token.getValue(), accessDurationSec).toString()); // отправка токенов как куки в заголовок HTTP
    }

    private void addRefreshTokenCookie(HttpHeaders headers, Token token) {
        logger.debug("Adding Refresh token cookie");
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshCookie(token.getValue(), refreshDurationSec).toString());
    }

    private void revokeAllTokens(User user) { // удаление/деактивация всех токенов пользователя
        Set <Token> tokens = user.getTokens();
        
        tokens.forEach(token -> {if(token.getExpiringDate().isBefore(LocalDateTime.now())) tokenRepository.delete(token); // удаление токена из БД по истечению времени жизни
        else if(!token.isDisabled()) {
            token.setDisabled(true); // если активен - сделать неактивным
            tokenRepository.save(token); // запись в БД обновлённого токена
        }});
        logger.debug(String.format("User's (name: %s) tokens was revocated", user.getUsername()));
    }

    public ResponseEntity<LoginResponseDto> login(LoginRequestDto request, String access, String refresh) {
        logger.info("Start login operation");
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            request.username(), request.password()));
        User user = userService.getUser(request.username());
        logger.debug("User with ID {} and name {} is founded and authentificated successfully", user.getId(), user.getUsername());

        boolean accessValid = jwtTokenProvider.isValid(access);
        boolean refreshValid = jwtTokenProvider.isValid(refresh);
        logger.debug("Tokens validity: Access - {}, Refresh - {}", accessValid, refreshValid);

        HttpHeaders headers = new HttpHeaders();

        revokeAllTokens(user);

        if (!accessValid) {
            Token newAccess = jwtTokenProvider.generatedAccessToken(Map.of("role", user.getRole().getAuthority()),
            accessDurationMin, ChronoUnit.MINUTES, user);
            logger.debug("New Access token created with parametrs: token type-{}, expiring date-{}", newAccess.getType(), newAccess.getExpiringDate());

            newAccess.setUser(user);
            logger.debug("Access token recorded to user {}", user.getUsername());
            addAccessTokenCookie(headers, newAccess);
            tokenRepository.save(newAccess);
            logger.debug("Access token with ID {} saved", newAccess.getId());
        }

        if (!refreshValid || accessValid) {
            Token newRefresh = jwtTokenProvider.generatedRefreshToken(refreshDurationDate, ChronoUnit.DAYS, user);
            logger.debug("New Refresh token created with parametrs: token type-{}, expiring date-{}", newRefresh.getType(), newRefresh.getExpiringDate());

            newRefresh.setUser(user);
            logger.debug("Refresh token recorded to user {}", user.getUsername());
            addRefreshTokenCookie(headers, newRefresh);
            tokenRepository.save(newRefresh);
            logger.debug("Refresh token with ID {} saved", newRefresh.getId());
        }

        SecurityContextHolder.getContext().setAuthentication(authentication); // устанавливает для текущего запроса аутентифицированного пользователя
        logger.info("Login completed successfully for user {}", user.getUsername());
        
        if (user.getTgBotChatId() != null) botService.sendMessage(user.getTgBotChatId().toString(), "Someone logged by your account " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));

        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(true, user.getRole().getName()));
    }

    public ResponseEntity<LoginResponseDto> refresh(String refreshToken){ // обновление access токена
        logger.info("Start refresh operation");
        if(!jwtTokenProvider.isValid(refreshToken)){
            logger.warn("Refresh token failed: token is invalid");
            throw new RuntimeException("token is invalid");
        }

        User user = userService.getUser(jwtTokenProvider.getUsername(refreshToken));
        logger.debug("User with ID {} and name {} is founded successfully", user.getId(), user.getUsername());

        Token newAccess = jwtTokenProvider.generatedAccessToken(Map.of("role", user.getRole().getAuthority()),
                accessDurationMin, ChronoUnit.MINUTES, user);
        logger.debug("New Access token created with parametrs: token type-{}, expiring date-{}, userId-{}", newAccess.getType(), newAccess.getExpiringDate(), user.getId());

        newAccess.setUser(user);
        logger.debug("Access token recorded to user {}", user.getUsername());
        HttpHeaders headers = new HttpHeaders();
        addAccessTokenCookie(headers, newAccess);
        tokenRepository.save(newAccess);
        logger.debug("Access token with ID {} saved", newAccess.getId());

        LoginResponseDto loginResponseDto = new LoginResponseDto(true, user.getRole().getName());
        logger.info("Access token successfully refreshed");
        return ResponseEntity.ok().headers(headers).body(loginResponseDto);
    }

    public ResponseEntity<LoginResponseDto> logout(String accessToken){
        logger.info("Start logout operation");
        SecurityContextHolder.clearContext();
        User user = userService.getUser(jwtTokenProvider.getUsername(accessToken));
        revokeAllTokens(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshCookie().toString());
        logger.info("Logout completed successfully for user {}", user.getUsername());
        
        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(false, null));
    }

    public UserLoggedDto info(){
        logger.info("Start get-info operation");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof AnonymousAuthenticationToken){
            logger.warn("User isn't authentificated");
            throw new RuntimeException("No user");
        }
        User user = userService.getUser(authentication.getName());
        logger.info("Info sended successfully about user {}", user.getUsername());
        return UserMapper.userToUserLoggedDto(user);
    }

    public ResponseEntity<LoginResponseDto> changePassword(ChangePasswordDto request) {
        logger.info("Start change-password operation");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof AnonymousAuthenticationToken){
            logger.warn("User isn't authentificated and cann't change password");
            throw new RuntimeException("No user");
        }
        User user = userService.getUser(authentication.getName());
        logger.debug("User with ID {} and name {} is founded", user.getId(), user.getUsername());

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            logger.warn("Failed change password (Wrong current password) for user: id-{}, name-{}", user.getId(), user.getUsername());
            throw new BadCredentialsException("Wrong current password");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            logger.warn("Failed change password (Equal new and current passwords) for user: id-{}, name-{}", user.getId(), user.getUsername());
            throw new BadCredentialsException("New password can't be equal to old password");
        }
        if (!request.newPassword().equals(request.newPasswordAgain())) {
            logger.warn("Failed change password (New passwords don't match) for user {}", user.getUsername());
            throw new BadCredentialsException("New passwords don't match");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userService.saveUser(user);
        logger.debug("Password changes saved for user {}", user.getUsername());
        botService.sendMessageToAdmin("Password of user " + user.getUsername() + " was changed");
        if (user.getTgBotChatId() != null) botService.sendMessage(user.getTgBotChatId().toString(), "Your password was changed at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));

        SecurityContextHolder.clearContext(); // убирает для текущего запроса аутентифицированного пользователя
        revokeAllTokens(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshCookie().toString());
        logger.info("Password changed successfully for user {}", user.getUsername());
        
        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(false, null));
    }

    public ResponseEntity<LoginResponseDto> changeTgChatId(ChangeTgChatIdDto request) {
        logger.info("Start change Telegram Bot chat Id operation");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof AnonymousAuthenticationToken){
            logger.warn("User isn't authentificated and cann't change Telegram Bot chat Id");
            throw new RuntimeException("No user");
        }
        User user = userService.getUser(authentication.getName());
        logger.debug("User with ID {} and name {} is founded", user.getId(), user.getUsername());

        if (!passwordEncoder.matches(request.Password(), user.getPassword())) {
            logger.warn("Failed change password (Wrong password) for user: id-{}, name-{}", user.getId(), user.getUsername());
            throw new BadCredentialsException("Wrong password");
        }

        Long oldChatId = user.getTgBotChatId();
        user.setTgBotChatId(request.newChatId());
        userService.saveUser(user);
        logger.debug("Chat Id changes saved for user {}", user.getUsername());

        botService.sendMessageToAdmin("Chat Id of user " + user.getUsername() + " was changed from " + oldChatId.toString() + " to " + user.getTgBotChatId().toString());

        SecurityContextHolder.clearContext(); // убирает для текущего запроса аутентифицированного пользователя
        revokeAllTokens(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshCookie().toString());
        logger.info("Chat Id changed successfully for user {}", user.getUsername());
        
        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(false, null));
    }
}
