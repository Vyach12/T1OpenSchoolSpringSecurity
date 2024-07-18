package openschool.java.security.authentication.service;

import lombok.RequiredArgsConstructor;
import openschool.java.security.authentication.dto.AuthenticationOperationResultTo;
import openschool.java.security.authentication.exception.InvalidUsernameOrPasswordException;
import openschool.java.security.security.jwt.JwtTokenGenerator;
import openschool.java.security.user.domain.UserRepository;
import openschool.java.security.user.dto.UserTo;
import openschool.java.security.user.exception.UserNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case для аутентификации пользователей.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationUseCase {
    /**
     * Репозиторий для пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Класс для генерации JWT-токенов.
     */
    private final JwtTokenGenerator tokenGenerator;

    /**
     * AuthenticationManager.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Аутентифицировать пользователя.
     *
     * @param user - данные пользователя для аутентификации
     * @return результат операции с токеном
     */
    @Transactional(readOnly = true)
    public AuthenticationOperationResultTo authenticate(final UserTo user) {
        checkUsernameExists(user.getUsername());

        try {
            var authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getUsername(), user.getPassword()
            );

            authenticationManager.authenticate(authenticationToken);
            return userRepository.findByUsername(user.getUsername())
                .map(entity -> AuthenticationOperationResultTo.builder()
                    .userId(entity.getId())
                    .token(tokenGenerator.generate(entity))
                    .build())
                .orElseThrow(() -> new UserNotFoundException(
                    String.format("Пользователь с username %s не найден", user.getUsername())));
        } catch (Exception ignored) {
            throw new InvalidUsernameOrPasswordException("Некорректный логин или пароль");
        }
    }

    /**
     * Проверить наличие пользователя по username.
     *
     * @param username - username
     * @throws UserNotFoundException - если username не найден
     */
    private void checkUsernameExists(final String username) throws UserNotFoundException {
        if (!userRepository.existsByUsername(username)) {
            throw new UserNotFoundException(
                String.format("Пользователь с username %s не найден", username));
        }
    }
}
