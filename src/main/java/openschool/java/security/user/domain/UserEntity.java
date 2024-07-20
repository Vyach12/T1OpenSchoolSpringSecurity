package openschool.java.security.user.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Сущность пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "user_entity")
public final class UserEntity implements UserDetails {
    /**
     * Идентификатор.
     */
    @Id
    @UuidGenerator
    private UUID id;

    /**
     * Уникальное имя пользователя.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Хэшированный пароль.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Роль.
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.USER;

    /**
     * Имя.
     */
    private String firstName;

    /**
     * Фамилия.
     */
    private String lastName;

    /**
     * Истекло ли время действия аккаунта.
     */
    @Builder.Default
    private boolean accountNonExpired = true;

    /**
     * Заблокирован ли аккаунт.
     */
    @Builder.Default
    private boolean accountNonLocked = true;

    /**
     * Истекло ли время жизни credentials.
     */
    @Builder.Default
    private boolean credentialsNonExpired = true;

    /**
     * Активен ли аккаунт.
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Дата и время создания.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime createdAt;

    /**
     * Дата и время последнего обновления.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime updatedAt;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }
}
