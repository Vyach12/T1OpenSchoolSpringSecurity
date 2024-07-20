package openschool.java.security.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import openschool.java.security.user.dto.UserTo;
import openschool.java.security.user.service.UserFindUseCase;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для взаимодействия с пользователями.
 */
@RestController
@CrossOrigin
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    /**
     * Use case поиска пользователей.
     */
    private final UserFindUseCase userFindUseCase;

    /**
     * Получение пользователя по username.
     *
     * @param username - username
     * @return найденный пользователь
     */
    @GetMapping("/{username}")
    @Operation(summary = "Получение пользователя по username")
    public UserTo findUserByUsername(final @PathVariable String username) {
        return userFindUseCase.findUserByUsername(username);
    }

    @GetMapping
    @Operation(summary = "Получение всех пользователей")
    public List<UserTo> findAllUsers() {
        return userFindUseCase.findAll();
    }
}
