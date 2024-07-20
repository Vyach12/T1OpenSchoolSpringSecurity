package openschool.java.security.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Класс для извлечения данных из JWT-токенов.
 */
@Service
@RequiredArgsConstructor
public class JwtTokenExtractor {
    /**
     * Секретный ключ.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Извлечь subject из токена.
     *
     * @param token - токен
     * @return subject
     */
    public String extractSubject(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Извлечь окончание времени жизни из токена.
     *
     * @param token - токен
     * @return окончание времени жизни токена
     */
    public Date extractExpiration(final String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлечь claim из токена.
     *
     * @param token          - токен
     * @param claimsResolver - функция для преобразования claim
     * @param <T>            - параметр
     * @return claim
     */
    private <T> T extractClaim(final String token,
                               Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Извлечь все claims из токена.
     *
     * @param token - токен
     * @return claims
     */
    private Claims extractAllClaims(final String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
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
