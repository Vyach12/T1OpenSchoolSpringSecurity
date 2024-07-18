package openschool.java.security.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Репозиторий для пользователей.
 */
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    /**
     * Поиск пользователя по username.
     *
     * @param username - username
     * @return найденный пользователь
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Существует ли пользователь по username.
     *
     * @param username - username
     * @return true, если существует, иначе false
     */
    boolean existsByUsername(String username);

    /**
     * Найти всех пользователей по идентификаторам.
     *
     * @param ids - идентификаторы
     * @return пользователи
     */
    List<UserEntity> findAllByIdIn(Set<UUID> ids);
}
