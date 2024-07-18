package openschool.java.security.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Класс для валидации JWT-токенов.
 */
@Service
@RequiredArgsConstructor
public class JwtTokenValidator {
    /**
     * Класс для извлечения данных из JWT-токенов.
     */
    private final JwtTokenExtractor extractor;

    /**
     * Является ли токен валидным для пользователя.
     *
     * @param token       - токен
     * @param userDetails - данные пользователя
     * @return является ли токен валидным
     */
    public boolean isValid(final String token,
                           final UserDetails userDetails) {
        String extractedUsername = extractor.extractUsername(token);
        return extractedUsername.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Истекло ли время жизни токена.
     *
     * @param token - токен
     * @return true, если время жизни истекло, иначе false
     */
    private boolean isTokenExpired(final String token) {
        return extractor.extractExpiration(token).before(new Date(System.currentTimeMillis()));
    }
}
