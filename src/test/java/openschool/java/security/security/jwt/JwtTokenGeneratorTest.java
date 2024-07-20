package openschool.java.security.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import openschool.java.security.user.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenGeneratorTest {

    private JwtTokenGenerator jwtTokenGenerator;
    private UserEntity userEntity;
    private Key key;

    @BeforeEach
    void setUp() {
        jwtTokenGenerator = new JwtTokenGenerator();
        ReflectionTestUtils.setField(jwtTokenGenerator, "accessTokenExpirationTimeSeconds", 3600L);
        ReflectionTestUtils.setField(jwtTokenGenerator, "refreshTokenExpirationTimeSeconds", 7200L);
        String base64Secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        ReflectionTestUtils.setField(jwtTokenGenerator, "secret", base64Secret);
        ReflectionTestUtils.setField(jwtTokenGenerator, "refreshTokenNameCookie", "refreshTokenCookie");

        userEntity = new UserEntity();
        userEntity.setUsername("testUser");

        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }

    @Test
    void generateAccessToken_ShouldCreateValidToken() {
        String accessToken = jwtTokenGenerator.generate(userEntity);
        assertNotNull(accessToken);

        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken);
        assertEquals(userEntity.getUsername(), claims.getBody().getSubject());

        long expectedExpirationTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3600);
        long actualExpirationTime = claims.getBody().getExpiration().getTime();
        assertTrue(Math.abs(expectedExpirationTime - actualExpirationTime) < 1000);
    }

    @Test
    void generateRefreshToken_ShouldCreateValidToken() {
        String refreshToken = jwtTokenGenerator.generateRefreshToken(userEntity);
        assertNotNull(refreshToken);

        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(refreshToken);
        assertEquals(userEntity.getUsername(), claims.getBody().getSubject());

        long expectedExpirationTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(7200);
        long actualExpirationTime = claims.getBody().getExpiration().getTime();
        assertTrue(Math.abs(expectedExpirationTime - actualExpirationTime) < 1000);
    }

    @Test
    void createRefreshTokenCookie_ShouldCreateValidCookie() {
        String refreshToken = jwtTokenGenerator.generateRefreshToken(userEntity);
        ResponseCookie cookie = jwtTokenGenerator.createRefreshTokenCookie(refreshToken);

        assertEquals("refreshTokenCookie", cookie.getName());
        assertEquals(refreshToken, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.isSecure());
        assertEquals("/api/v1/auth", cookie.getPath());
        assertEquals(7200, cookie.getMaxAge().getSeconds());
    }
}