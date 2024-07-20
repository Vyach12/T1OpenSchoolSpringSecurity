package openschool.java.security.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenExtractorTest {

    private JwtTokenExtractor jwtTokenExtractor;
    private Key key;
    private String validToken;

    @BeforeEach
    void setUp() {
        jwtTokenExtractor = new JwtTokenExtractor();
        String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        ReflectionTestUtils.setField(jwtTokenExtractor, "secret", secret);

        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

        // Create a valid token for testing
        validToken = Jwts.builder()
                .setSubject("testUser")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void extractSubject_WithValidToken_ShouldReturnCorrectSubject() {
        String subject = jwtTokenExtractor.extractSubject(validToken);
        assertEquals("testUser", subject);
    }

    @Test
    void extractExpiration_WithValidToken_ShouldReturnCorrectExpiration() {
        Date expiration = jwtTokenExtractor.extractExpiration(validToken);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void extractSubject_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalidToken";
        assertThrows(Exception.class, () -> jwtTokenExtractor.extractSubject(invalidToken));
    }

    @Test
    void extractExpiration_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalidToken";
        assertThrows(Exception.class, () -> jwtTokenExtractor.extractExpiration(invalidToken));
    }

    @Test
    void extractExpiration_WithExpiredToken_ShouldThrowException() {
        String expiredToken = Jwts.builder()
                .setSubject("expiredUser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000 * 60 * 60))
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // 1 hour ago
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThrows(Exception.class, () -> jwtTokenExtractor.extractExpiration(expiredToken));
    }

    @Test
    void extractSubject_WithModifiedToken_ShouldThrowException() {
        String modifiedToken = validToken.substring(0, validToken.length() - 1) + "X";
        assertThrows(Exception.class, () -> jwtTokenExtractor.extractSubject(modifiedToken));
    }

    @Test
    void extractExpiration_WithModifiedToken_ShouldThrowException() {
        String modifiedToken = validToken.substring(0, validToken.length() - 1) + "X";
        assertThrows(Exception.class, () -> jwtTokenExtractor.extractExpiration(modifiedToken));
    }

    @Test
    void extractSubject_WithTokenSignedWithDifferentKey_ShouldThrowException() {
        Key differentKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String tokenWithDifferentKey = Jwts.builder()
                .setSubject("testUser")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(differentKey, SignatureAlgorithm.HS256)
                .compact();

        assertThrows(Exception.class, () -> jwtTokenExtractor.extractSubject(tokenWithDifferentKey));
    }

    @Test
    void extractExpiration_WithTokenSignedWithDifferentKey_ShouldThrowException() {
        Key differentKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String tokenWithDifferentKey = Jwts.builder()
                .setSubject("testUser")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(differentKey, SignatureAlgorithm.HS256)
                .compact();

        assertThrows(Exception.class, () -> jwtTokenExtractor.extractExpiration(tokenWithDifferentKey));
    }
}