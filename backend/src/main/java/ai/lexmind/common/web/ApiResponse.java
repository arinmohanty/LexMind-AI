package ai.lexmind.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

/**
 * Standard API envelope: {@code { data, error, traceId }} (LLD §5).
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(T data, ApiError error, String traceId) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, null, UUID.randomUUID().toString());
    }

    public static <T> ApiResponse<T> fail(ApiError error) {
        return new ApiResponse<>(null, error, UUID.randomUUID().toString());
    }
}
