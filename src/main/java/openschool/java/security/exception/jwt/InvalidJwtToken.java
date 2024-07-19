package openschool.java.security.exception.jwt;

public class InvalidJwtToken extends RuntimeException {
    public InvalidJwtToken(String message) {
        super(message);
    }
}
