package openschool.java.security.user.service;

import openschool.java.security.exception.user.UserNotFoundException;
import openschool.java.security.user.domain.UserEntity;
import openschool.java.security.user.domain.UserRepository;
import openschool.java.security.user.dto.UserTo;
import openschool.java.security.user.mapping.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFindUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    private UserFindUseCase userFindUseCase;

    @BeforeEach
    void setUp() {
        userFindUseCase = new UserFindUseCase(userRepository, userMapper);
    }

    @Test
    void findUserByUsername_ExistingUser_ReturnsUserTo() {
        String username = "testUser";
        UserEntity userEntity = new UserEntity();
        UserTo userTo = new UserTo();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(userMapper.mapFromEntity(userEntity)).thenReturn(userTo);

        UserTo result = userFindUseCase.findUserByUsername(username);

        assertNotNull(result);
        assertEquals(userTo, result);
        verify(userRepository).findByUsername(username);
        verify(userMapper).mapFromEntity(userEntity);
    }

    @Test
    void findUserByUsername_NonExistingUser_ThrowsUserNotFoundException() {
        String username = "nonExistingUser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userFindUseCase.findUserByUsername(username));
        verify(userRepository).findByUsername(username);
        verify(userMapper, never()).mapFromEntity(any());
    }

    @Test
    void findAll_ReturnsListOfUserTo() {
        UserEntity user1 = new UserEntity();
        UserEntity user2 = new UserEntity();
        List<UserEntity> userEntities = Arrays.asList(user1, user2);

        UserTo userTo1 = new UserTo();
        UserTo userTo2 = new UserTo();

        when(userRepository.findAll()).thenReturn(userEntities);
        when(userMapper.mapFromEntity(user1)).thenReturn(userTo1);
        when(userMapper.mapFromEntity(user2)).thenReturn(userTo2);

        List<UserTo> result = userFindUseCase.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(userTo1));
        assertTrue(result.contains(userTo2));
        verify(userRepository).findAll();
        verify(userMapper, times(2)).mapFromEntity(any());
    }

    @Test
    void findAll_EmptyList_ReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserTo> result = userFindUseCase.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
        verify(userMapper, never()).mapFromEntity(any());
    }
}