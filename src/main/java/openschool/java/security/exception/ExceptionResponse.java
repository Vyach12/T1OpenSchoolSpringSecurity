package openschool.java.security.exception;

import lombok.Builder;

@Builder
public record ExceptionResponse(
        String msg
) {
}
