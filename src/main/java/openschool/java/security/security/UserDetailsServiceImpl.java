package openschool.java.security.security;

import lombok.RequiredArgsConstructor;
import openschool.java.security.user.service.UserFindUseCase;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Класс для получения данных о пользователях.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserFindUseCase userFindUseCase;

    /**
     * Получить данные о пользователе по его username.
     *
     * @param username - username
     * @return данные о пользователе
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        return userFindUseCase.findUserByUsername(username);
    }
}
