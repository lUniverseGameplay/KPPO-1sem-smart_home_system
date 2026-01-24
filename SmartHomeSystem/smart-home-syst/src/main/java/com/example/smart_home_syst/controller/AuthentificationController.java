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
import com.example.smart_home_syst.dto.ChangeTgChatIdDto;
import com.example.smart_home_syst.dto.LoginRequestDto;
import com.example.smart_home_syst.dto.LoginResponseDto;
import com.example.smart_home_syst.dto.UserLoggedDto;
import com.example.smart_home_syst.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(
    name = "Аутентификация",
    description = "Модуль для аутентификации пользователя. Ссылка на бота для получения уведомлений: @kppo_pinj4_smarthomesyst_bot"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthentificationController {
    private final AuthenticationService authenticationService;

    @Operation(
    summary = "Вход в систему",
    description = """
    Аутентифицирует пользователя по логину и паролю.
    \nusername - имя пользователя
    \npassword - пароль пользователя
    """)
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login (
        @CookieValue(name="access-token",required = false) String accessToken,
        @CookieValue(name="refresh-token",required = false) String refreshToken,
        @RequestBody LoginRequestDto request){
            return authenticationService.login(request,accessToken,refreshToken);
    }

    @Operation(
    summary = "Обновление Access токена",
    description = "Обновление токена происходит при отправке запроса, ничего вводить не требуется")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(
    @CookieValue(name = "refresh-token", required = false) String refresh) {
        return authenticationService.refresh(refresh);
    }

    @Operation(
    summary = "Выход из системы",
    description = "Аутентифицироанный пользователь завершит текущую сессию")
    @PostMapping("/logout")
    public ResponseEntity<LoginResponseDto> logout(
    @CookieValue(name = "access-token", required = false) String access) {
        return authenticationService.logout(access);
    }

    @Operation(
    summary = "Информация о текущем пользователе",
    description = "Будет получена информация об аутентифицированном пользователе")
    @GetMapping("/info")
    public ResponseEntity <UserLoggedDto> info() {
        return ResponseEntity.ok(authenticationService.info());
    }

    @Operation(
    summary = "Смена пароля",
    description = """
    Заполните все поля для смены пароля текущего пользователя.
    \noldPassword - текущий пароль для подтверждения
    \nnewPassword - новый пароль для замены
    \nnewPasswordAgain - подтверждение нового пароля
    \nПравила смены пароля:
    \nЗначение поля oldPassword должно совпадать с текущим паролем
    \nЗначение поля newPassword должно отличаться от текущего пароля
    \nЗначение поля newPassword и newPasswordAgain поля должно совпадать
    """)
    @PatchMapping("/changePassword")
    public ResponseEntity <LoginResponseDto> changePassword(ChangePasswordDto request) {
        return authenticationService.changePassword(request);
    }

    @Operation(
    summary = "Смена чата ТГ",
    description = """
    Заполните все поля для смены Id чата с ТГ ботом.
    \nnewChatId - номер чата с ботом (запросите для этого номер у бота через команду '/chat_id')
    \nPassword - текущий пароль для подтверждения
    """)
    @PatchMapping("/changeTgChat")
    public ResponseEntity<LoginResponseDto> changeTgChatNumber(ChangeTgChatIdDto request) {
        return authenticationService.changeTgChatId(request);
    }

}
