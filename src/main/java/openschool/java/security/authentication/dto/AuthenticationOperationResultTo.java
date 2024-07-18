package openschool.java.security.authentication.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AuthenticationOperationResultTo(
        UUID userId,
        String token
) {
}
