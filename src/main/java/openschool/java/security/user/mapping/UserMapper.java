package openschool.java.security.user.mapping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import openschool.java.security.user.domain.UserEntity;
import openschool.java.security.user.dto.UserTo;
import org.mapstruct.Mapper;

/**
 * Маппер для пользователей.
 */
@Getter
@Mapper(componentModel = "spring")
@RequiredArgsConstructor
public abstract class UserMapper {
    /**
     * Маппинг из entity в to.
     *
     * @param entity - сущность
     * @return to с проставленными полями
     */
    public abstract UserTo mapFromEntity(UserEntity entity);

    /**
     * Маппинг из to в entity.
     *
     * @param to - to-модель пользователя
     * @return сущность с проставленными полями
     */
    public abstract UserEntity mapFromTo(UserTo to);
}
