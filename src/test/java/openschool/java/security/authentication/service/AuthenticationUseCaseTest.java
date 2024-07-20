package openschool.java.security.authentication.service;

import openschool.java.security.authentication.domain.RefreshTokenRepository;
import openschool.java.security.authentication.dto.AuthenticationOperationResultTo;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationUseCaseTest {

    @InjectMocks
    private AuthenticationUseCase authenticationUseCase;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtTokenExtractor jwtTokenExtractor;

    @Mock
    private JwtTokenGenerator jwtTokenGenerator;

    @Mock
    private JwtTokenValidator jwtTokenValidator;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    private UserTo userTo;
    private UserEntity userEntity;
    private String refreshToken;
    private ResponseCookie responseCookie;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        userTo = UserTo.builder()
                .id(userId)
                .username("testuser")
                .password("testpassword")
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        userEntity = UserEntity.builder()
                .id(userId)
                .username("testuser")
                .password("encodedpassword")
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        refreshToken = "newRefreshToken";
        responseCookie = ResponseCookie.from("refresh-token", refreshToken)
                .httpOnly(true)
                .maxAge(3600)
                .secure(true)
                .path("/api/v1/auth")
                .build();
    }

    @Test
    void authenticate_Success() {
        when(userRepository.existsByUsername(any(String.class))).thenReturn(true);
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(userEntity));
        when(jwtTokenGenerator.generateRefreshToken(any(UserEntity.class))).thenReturn(refreshToken);
        when(jwtTokenGenerator.createRefreshTokenCookie(any(String.class))).thenReturn(responseCookie);
        when(jwtTokenGenerator.generate(any(UserEntity.class))).thenReturn("jwtToken");

        ResponseEntity<AuthenticationOperationResultTo> response = authenticationUseCase.authenticate(userTo);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
        assertNotNull(response.getBody());
        assertEquals(userEntity.getId(), response.getBody().userId());
        assertEquals("jwtToken", response.getBody().token());
    }

    @Test
    void authenticate_ThrowsUserNotFoundException() {
        when(userRepository.existsByUsername(any(String.class))).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> authenticationUseCase.authenticate(userTo));
    }

    @Test
    void register_Success() {
        when(userRepository.existsByUsername(any(String.class))).thenReturn(false);
        when(userMapper.mapForRegistration(any(UserTo.class))).thenReturn(userEntity);
        when(jwtTokenGenerator.generateRefreshToken(any(UserEntity.class))).thenReturn(refreshToken);
        when(jwtTokenGenerator.createRefreshTokenCookie(any(String.class))).thenReturn(responseCookie);
        when(jwtTokenGenerator.generate(any(UserEntity.class))).thenReturn("jwtToken");

        ResponseEntity<AuthenticationOperationResultTo> response = authenticationUseCase.register(userTo);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
        assertNotNull(response.getBody());
        assertEquals(userEntity.getId(), response.getBody().userId());
        assertEquals("jwtToken", response.getBody().token());
    }

    @Test
    void register_ThrowsUserAlreadyExistsException() {
        when(userRepository.existsByUsername(any(String.class))).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authenticationUseCase.register(userTo));
    }

    @Test
    void refreshToken_Success() {
        when(jwtTokenExtractor.extractSubject(any(String.class))).thenReturn(userEntity.getUsername());
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(userEntity));
        when(jwtTokenValidator.isValid(any(String.class), any(UserEntity.class))).thenReturn(true);
        when(jwtTokenGenerator.generateRefreshToken(any(UserEntity.class))).thenReturn(refreshToken);
        when(jwtTokenGenerator.createRefreshTokenCookie(any(String.class))).thenReturn(responseCookie);
        when(jwtTokenGenerator.generate(any(UserEntity.class))).thenReturn("jwtToken");

        ResponseEntity<AuthenticationOperationResultTo> response = authenticationUseCase.refreshToken("oldRefreshToken");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
        assertNotNull(response.getBody());
        assertEquals(userEntity.getId(), response.getBody().userId());
        assertEquals("jwtToken", response.getBody().token());
    }

    @Test
    void refreshToken_ThrowsInvalidJwtTokenException() {
        when(jwtTokenExtractor.extractSubject(any(String.class))).thenReturn(userEntity.getUsername());
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(userEntity));
        when(jwtTokenValidator.isValid(any(String.class), any(UserEntity.class))).thenReturn(false);

        assertThrows(InvalidJwtToken.class, () -> authenticationUseCase.refreshToken("oldRefreshToken"));
    }
}
