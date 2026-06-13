package ai.lexmind.auth.api;

import ai.lexmind.auth.api.dto.AuthDtos.AuthResponse;
import ai.lexmind.auth.api.dto.AuthDtos.ForgotPasswordRequest;
import ai.lexmind.auth.api.dto.AuthDtos.LoginRequest;
import ai.lexmind.auth.api.dto.AuthDtos.RefreshRequest;
import ai.lexmind.auth.api.dto.AuthDtos.RegisterRequest;
import ai.lexmind.auth.api.dto.AuthDtos.ResetPasswordRequest;
import ai.lexmind.auth.api.dto.AuthDtos.UserDto;
import ai.lexmind.auth.service.AuthService;
import ai.lexmind.common.security.SecurityUtils;
import ai.lexmind.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Registration, login, token refresh, password reset")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new account and receive tokens")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive access + refresh tokens")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a refresh token for a new token pair (rotation)")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ApiResponse.ok(authService.refresh(req.refreshToken()));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password-reset token (always returns success)")
    public ApiResponse<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req.email());
        return ApiResponse.ok(Map.of("message", "If the email exists, a reset link has been sent"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using a valid reset token")
    public ApiResponse<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.token(), req.newPassword());
        return ApiResponse.ok(Map.of("message", "Password updated successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user")
    public ApiResponse<UserDto> me() {
        return ApiResponse.ok(authService.getCurrentUser(SecurityUtils.currentUser().getId()));
    }
}
