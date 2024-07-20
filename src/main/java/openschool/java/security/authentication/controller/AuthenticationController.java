package openschool.java.security.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import openschool.java.security.authentication.dto.AuthenticationOperationResultTo;
import openschool.java.security.authentication.service.AuthenticationUseCase;
import openschool.java.security.user.dto.UserTo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для регистрации и аутентификации.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "AuthenticationController", description = "Контроллер для регистрации и аутентификации")
public class AuthenticationController {
    /**
     * Use case для аутентификации пользователей.
     */
    private final AuthenticationUseCase authenticationUseCase;

    /**
     * Запрос на регистрацию пользователя.
     *
     * @param user - данные пользователя для регистрации
     * @return результат операции с токеном
     */
    @PostMapping("/register")
    @Operation(summary = "Запрос на регистрацию пользователя")
    public ResponseEntity<AuthenticationOperationResultTo> register(final @Valid @RequestBody UserTo user) {
        return authenticationUseCase.register(user);
    }

    /**
     * Запрос на аутентификацию пользователя.
     *
     * @param user - данные пользователя для аутентификации
     * @return результат операции с токеном
     */
    @PostMapping("/authenticate")
    @Operation(summary = "Запрос на аутентификацию пользователя")
    public ResponseEntity<AuthenticationOperationResultTo> authenticate(final @Valid @RequestBody UserTo user) {
        return authenticationUseCase.authenticate(user);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Запрос на обновлнеие токена")
    public ResponseEntity<AuthenticationOperationResultTo> refresh(
            @CookieValue("${jwt.refresh.name}") String refreshToken) {
        return authenticationUseCase.refreshToken(refreshToken);
    }
}
