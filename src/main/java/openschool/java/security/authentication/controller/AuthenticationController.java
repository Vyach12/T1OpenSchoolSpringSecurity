package openschool.java.security.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import openschool.java.security.authentication.dto.AuthenticationOperationResultTo;
import openschool.java.security.authentication.service.AuthenticationUseCase;
import openschool.java.security.authentication.service.RegistrationUseCase;
import openschool.java.security.user.dto.UserTo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для регистрации и аутентификации.
 */
@RestController
@CrossOrigin
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "AuthenticationController", description = "Контроллер для регистрации и аутентификации")
public class AuthenticationController {
    /**
     * Use case для аутентификации пользователей.
     */
    private final AuthenticationUseCase authenticationUseCase;

    /**
     * Use case для регистрации пользователей.
     */
    private final RegistrationUseCase registrationUseCase;

    /**
     * Запрос на регистрацию пользователя.
     *
     * @param user - данные пользователя для регистрации
     * @return результат операции с токеном
     */
    @PostMapping("/register")
    @Operation(summary = "Запрос на регистрацию пользователя")
    public AuthenticationOperationResultTo register(final @Valid @RequestBody UserTo user) {
        return registrationUseCase.register(user);
    }

    /**
     * Запрос на аутентификацию пользователя.
     *
     * @param user - данные пользователя для аутентификации
     * @return результат операции с токеном
     */
    @PostMapping("/authenticate")
    @Operation(summary = "Запрос на аутентификацию пользователя")
    public AuthenticationOperationResultTo authenticate(final @Valid @RequestBody UserTo user) {
        return authenticationUseCase.authenticate(user);
    }
}
