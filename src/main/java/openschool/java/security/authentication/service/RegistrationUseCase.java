package openschool.java.security.authentication.service;

import lombok.RequiredArgsConstructor;
import openschool.java.security.authentication.dto.AuthenticationOperationResultTo;
import openschool.java.security.authentication.exception.UserAlreadyExistsException;
import openschool.java.security.security.jwt.JwtTokenGenerator;
import openschool.java.security.user.domain.UserEntity;
import openschool.java.security.user.domain.UserRepository;
import openschool.java.security.user.dto.UserTo;
import openschool.java.security.user.mapping.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Use case для регистрации пользователей.
 */
@Service
@RequiredArgsConstructor
public class RegistrationUseCase {
    /**
     * Репозиторий для пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Маппер для пользователей.
     */
    private final UserMapper userMapper;

    /**
     * Класс для генерации JWT-токенов.
     */
    private final JwtTokenGenerator tokenGenerator;

    /**
     * Зарегистрировать пользователя.
     *
     * @param user - данные пользователя для регистрации
     * @return результат операции с токеном
     */
    @Transactional
    public AuthenticationOperationResultTo register(final UserTo user) {
        Optional<UserEntity> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException(
                    String.format("Пользователь с данным username %s уже существует в системе",
                            user.getUsername()));
        }

        UserEntity entity = userMapper.mapForRegistration(user);
        userRepository.save(entity);
        return AuthenticationOperationResultTo.builder()
            .userId(entity.getId())
            .token(tokenGenerator.generate(entity))
            .build();
    }
}
