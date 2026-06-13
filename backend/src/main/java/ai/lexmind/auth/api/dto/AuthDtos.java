package ai.lexmind.auth.api.dto;

import ai.lexmind.auth.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Request/response DTOs for the auth feature (grouped for cohesion). */
public final class AuthDtos {

    private AuthDtos() {}

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 150) String fullName,
            @NotBlank String role,                 // LAW_STUDENT | ADVOCATE | RESEARCHER | LAW_FIRM_ADMIN
            String organizationName                // optional (firm admin)
    ) {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record ForgotPasswordRequest(@Email @NotBlank String email) {}

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 8, max = 72) String newPassword
    ) {}

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UserDto user
    ) {}

    public record UserDto(
            UUID id,
            String email,
            String fullName,
            String role,
            UUID organizationId,
            String status,
            boolean emailVerified
    ) {
        public static UserDto from(User u) {
            return new UserDto(u.getId(), u.getEmail(), u.getFullName(),
                    u.getRole().getName(), u.getOrganizationId(),
                    u.getStatus().name(), u.isEmailVerified());
        }
    }
}
