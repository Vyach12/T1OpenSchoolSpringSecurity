package openschool.java.security.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenValidatorTest {

    @Mock
    private JwtTokenExtractor extractor;

    @Mock
    private UserDetails userDetails;

    private JwtTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new JwtTokenValidator(extractor);
    }

    @Test
    void isValid_WithValidTokenAndMatchingUsername_ReturnsTrue() {
        String token = "validToken";
        String username = "testUser";

        when(extractor.extractSubject(token)).thenReturn(username);
        when(userDetails.getUsername()).thenReturn(username);
        when(extractor.extractExpiration(token)).thenReturn(new Date(System.currentTimeMillis() + 1000000));

        assertTrue(validator.isValid(token, userDetails));
    }

    @Test
    void isValid_WithValidTokenButDifferentUsername_ReturnsFalse() {
        String token = "validToken";
        String extractedUsername = "testUser";
        String actualUsername = "differentUser";

        when(extractor.extractSubject(token)).thenReturn(extractedUsername);
        when(userDetails.getUsername()).thenReturn(actualUsername);

        assertFalse(validator.isValid(token, userDetails));
    }

    @Test
    void isValid_WithExpiredToken_ReturnsFalse() {
        String token = "expiredToken";
        String username = "testUser";

        when(extractor.extractSubject(token)).thenReturn(username);
        when(userDetails.getUsername()).thenReturn(username);
        when(extractor.extractExpiration(token)).thenReturn(new Date(System.currentTimeMillis() - 1000000));

        assertFalse(validator.isValid(token, userDetails));
    }
}