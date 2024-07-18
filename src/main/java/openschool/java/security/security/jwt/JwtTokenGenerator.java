package openschool.java.security.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
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
     * Время действия токена в секундах.
     */
    @Value("${jwt.expiration-time-seconds}")
    private Long expirationTimeSeconds;

    /**
     * Секретный ключ.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Сгенерировать токен для пользователя.
     *
     * @param userDetails - данные пользователя
     * @return токен
     */
    public String generate(final UserDetails userDetails) {
        return generateToken(Map.of(), userDetails);
    }

    /**
     * Сгенерировать токен.
     *
     * @param claims      - claims
     * @param userDetails - данные пользователя
     * @return токен
     */
    private String generateToken(final Map<String, Object> claims,
                                 final UserDetails userDetails) {
        return Jwts
            .builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
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
