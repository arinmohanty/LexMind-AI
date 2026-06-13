package ai.lexmind.common.error;

import org.springframework.http.HttpStatus;

/**
 * Domain exceptions mapped to HTTP status + stable error codes by the global handler.
 */
public final class AppExceptions {

    private AppExceptions() {}

    /** Base for all expected, mapped application errors. */
    public static abstract class AppException extends RuntimeException {
        private final HttpStatus status;
        private final String code;
        protected AppException(HttpStatus status, String code, String message) {
            super(message);
            this.status = status;
            this.code = code;
        }
        public HttpStatus getStatus() { return status; }
        public String getCode() { return code; }
    }

    public static class NotFoundException extends AppException {
        public NotFoundException(String code, String message) {
            super(HttpStatus.NOT_FOUND, code, message);
        }
    }

    public static class ConflictException extends AppException {
        public ConflictException(String code, String message) {
            super(HttpStatus.CONFLICT, code, message);
        }
    }

    public static class BadRequestException extends AppException {
        public BadRequestException(String code, String message) {
            super(HttpStatus.BAD_REQUEST, code, message);
        }
    }

    public static class ForbiddenException extends AppException {
        public ForbiddenException(String code, String message) {
            super(HttpStatus.FORBIDDEN, code, message);
        }
    }

    public static class UnauthorizedException extends AppException {
        public UnauthorizedException(String code, String message) {
            super(HttpStatus.UNAUTHORIZED, code, message);
        }
    }
}
