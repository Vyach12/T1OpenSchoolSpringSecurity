package openschool.java.security.authentication.service;

import lombok.RequiredArgsConstructor;
import openschool.java.security.authentication.domain.RefreshTokenEntity;
import openschool.java.security.authentication.domain.RefreshTokenRepository;
import openschool.java.security.authentication.dto.AuthenticationOperationResultTo;
import openschool.java.security.exception.auth.InvalidUsernameOrPasswordException;
import openschool.java.security.exception.auth.UserAlreadyExistsException;
import openschool.java.security.exception.jwt.InvalidJwtToken;
import openschool.java.security.exception.user.UserNotFoundException;
import openschool.java.security.security.jwt.JwtTokenExtractor;
import openschool.java.security.security.jwt.JwtTokenGenerator;
import openschool.java.security.security.jwt.JwtTokenValidator;
import openschool.java.security.user.domain.UserEntity;
import openschool.java.security.user.domain.UserRepository;
import openschool.java.security.user.dto.UserTo;
import openschool.java.security.user.mapping.UserMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для управления аутентификацией и регистрацией пользователей.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationUseCase {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtTokenExtractor jwtTokenExtractor;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final JwtTokenValidator jwtTokenValidator;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;

    /**
     * Аутентифицирует пользователя на основе предоставленных данных.
     *
     * @param userTo данные пользователя для аутентификации
     * @return результат операции аутентификации, включающий JWT токен
     */
    @Transactional(readOnly = true)
    public ResponseEntity<AuthenticationOperationResultTo> authenticate(final UserTo userTo) {
        validateUserExists(userTo.getUsername());
        authenticateUser(userTo);

        UserEntity userEntity = getUserEntity(userTo.getUsername());
        String refreshToken = createAndSaveRefreshToken(userEntity);
        ResponseCookie cookie = jwtTokenGenerator.createRefreshTokenCookie(refreshToken);

        return buildResponseEntity(userEntity, cookie);
    }

    /**
     * Регистрирует нового пользователя на основе предоставленных данных.
     *
     * @param userTo данные пользователя для регистрации
     * @return результат операции регистрации, включающий JWT токен
     */
    @Transactional
    public ResponseEntity<AuthenticationOperationResultTo> register(final UserTo userTo) {
        checkUserDoesNotExist(userTo.getUsername());

        UserEntity userEntity = userMapper.mapForRegistration(userTo);
        userRepository.save(userEntity);

        String refreshToken = createAndSaveRefreshToken(userEntity);
        ResponseCookie cookie = jwtTokenGenerator.createRefreshTokenCookie(refreshToken);

        return buildResponseEntity(userEntity, cookie);
    }

    /**
     * Обновляет refresh токен.
     *
     * @param oldRefreshToken старый refresh token
     * @return результат операции обновления токена
     */
    public ResponseEntity<AuthenticationOperationResultTo> refreshToken(final String oldRefreshToken) {
        String username = jwtTokenExtractor.extractSubject(oldRefreshToken);
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidJwtToken("Некорректный JWT токен"));

        if (!jwtTokenValidator.isValid(oldRefreshToken, userEntity)) {
            throw new InvalidJwtToken("Некорректный JWT токен");
        }

        String newRefreshToken = createAndSaveRefreshToken(userEntity);
        ResponseCookie cookie = jwtTokenGenerator.createRefreshTokenCookie(newRefreshToken);

        return buildResponseEntity(userEntity, cookie);
    }

    /**
     * Проверяет наличие пользователя по username.
     *
     * @param username имя пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    private void validateUserExists(final String username) throws UserNotFoundException {
        if (!userRepository.existsByUsername(username)) {
            throw new UserNotFoundException(
                    String.format("Пользователь с username %s не найден", username));
        }
    }

    /**
     * Аутентифицирует пользователя с помощью AuthenticationManager.
     *
     * @param userTo данные пользователя для аутентификации
     */
    private void authenticateUser(final UserTo userTo) {
        try {
            var authenticationToken = new UsernamePasswordAuthenticationToken(
                    userTo.getUsername(), userTo.getPassword()
            );
            authenticationManager.authenticate(authenticationToken);
        } catch (Exception ignored) {
            throw new InvalidUsernameOrPasswordException("Некорректный логин или пароль");
        }
    }

    /**
     * Получает сущность пользователя по username.
     *
     * @param username имя пользователя
     * @return сущность пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    private UserEntity getUserEntity(final String username) throws UserNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("Пользователь с username %s не найден", username)));
    }

    /**
     * Создает и сохраняет refresh токен для пользователя.
     *
     * @param userEntity сущность пользователя
     * @return новый refresh токен
     */
    private String createAndSaveRefreshToken(final UserEntity userEntity) {
        String refreshToken = jwtTokenGenerator.generateRefreshToken(userEntity);
        refreshTokenRepository.save(RefreshTokenEntity.builder()
                .userId(userEntity.getId())
                .value(refreshToken)
                .build());
        return refreshToken;
    }

    /**
     * Формирует ResponseEntity с результатом аутентификации или регистрации.
     *
     * @param userEntity сущность пользователя
     * @param cookie     cookie с refresh токеном
     * @return ResponseEntity с результатом операции
     */
    private ResponseEntity<AuthenticationOperationResultTo> buildResponseEntity(
            final UserEntity userEntity,
            final ResponseCookie cookie) {
        var response = AuthenticationOperationResultTo.builder()
                .userId(userEntity.getId())
                .token(jwtTokenGenerator.generate(userEntity))
                .build();

        var headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

        return new ResponseEntity<>(response, headers, HttpStatus.CREATED);
    }

    /**
     * Проверяет, что пользователь с заданным username не существует.
     *
     * @param username имя пользователя
     * @throws UserAlreadyExistsException если пользователь существует
     */
    private void checkUserDoesNotExist(final String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException(
                    String.format("Пользователь с данным username %s уже существует в системе", username));
        }
    }
}
