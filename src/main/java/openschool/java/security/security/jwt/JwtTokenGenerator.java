package openschool.java.security.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import openschool.java.security.user.domain.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Класс для генерации JWT-токенов.
 */
@Service
@RequiredArgsConstructor
public class JwtTokenGenerator {
    /**
     * Время действия access токена в секундах.
     */
    @Value("${jwt.expiration-time-seconds}")
    private Long accessTokenExpirationTimeSeconds;

    /**
     * Время действия refresh токена в секундах.
     */
    @Value("${jwt.refresh.expiration-time-seconds}")
    private Long refreshTokenExpirationTimeSeconds;

    /**
     * Секретный ключ.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Наименование refresh токена для cookie.
     */
    @Value("${jwt.refresh.name}")
    private String refreshTokenNameCookie;

    /**
     * Метод для генерации нового access-токена.
     *
     * @param userEntity - данные пользователя
     * @return токен
     */
    public String generate(final UserEntity userEntity) {
        return generateToken(Map.of(), userEntity, accessTokenExpirationTimeSeconds);
    }

    /**
     * Метод для генерации нового refresh-токена.
     *
     * @param userEntity - данные пользователя
     * @return сгенерированный refresh-токен
     */
    public String generateRefreshToken(final UserEntity userEntity) {
        return generateToken(Map.of(), userEntity, refreshTokenExpirationTimeSeconds);
    }

    /**
     * Метод для создания куки с refresh-токеном.
     *
     * @param refreshToken - значение refresh-токена
     * @return созданная куки
     */
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(refreshTokenNameCookie, refreshToken)
                .httpOnly(true)
                .maxAge(refreshTokenExpirationTimeSeconds)
                .secure(true)
                .path("/api/v1/auth")
                .build();
    }

    /**
     * Сгенерировать токен.
     *
     * @param claims     - claims
     * @param userEntity - данные пользователя
     * @return токен
     */
    private String generateToken(final Map<String, Object> claims,
                                 final UserEntity userEntity,
                                 long expirationTimeSeconds) {
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(userEntity.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()
                        + TimeUnit.SECONDS.toMillis(expirationTimeSeconds)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Получить закодированный секретный ключ.
     *
     * @return закодированный секретный ключ
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
