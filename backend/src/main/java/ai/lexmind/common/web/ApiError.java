package ai.lexmind.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** Error body carried inside {@link ApiResponse}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String code, String message, List<FieldError> details) {

    public ApiError(String code, String message) {
        this(code, message, null);
    }

    public record FieldError(String field, String message) {}
}
