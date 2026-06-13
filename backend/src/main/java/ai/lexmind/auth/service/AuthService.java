package ai.lexmind.auth.service;

import ai.lexmind.auth.api.dto.AuthDtos.AuthResponse;
import ai.lexmind.auth.api.dto.AuthDtos.LoginRequest;
import ai.lexmind.auth.api.dto.AuthDtos.RegisterRequest;
import ai.lexmind.auth.api.dto.AuthDtos.UserDto;
import ai.lexmind.auth.domain.PasswordResetToken;
import ai.lexmind.auth.domain.RefreshToken;
import ai.lexmind.auth.domain.Role;
import ai.lexmind.auth.domain.User;
import ai.lexmind.auth.repo.PasswordResetTokenRepository;
import ai.lexmind.auth.repo.RefreshTokenRepository;
import ai.lexmind.auth.repo.RoleRepository;
import ai.lexmind.auth.repo.UserRepository;
import ai.lexmind.common.audit.AuditService;
import ai.lexmind.common.config.LexMindProperties;
import ai.lexmind.common.error.AppExceptions.BadRequestException;
import ai.lexmind.common.error.AppExceptions.ConflictException;
import ai.lexmind.common.error.AppExceptions.UnauthorizedException;
import ai.lexmind.common.security.UserPrincipal;
import ai.lexmind.organization.Organization;
import ai.lexmind.organization.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final Set<String> SELF_REGISTERABLE =
            Set.of("LAW_STUDENT", "ADVOCATE", "RESEARCHER", "LAW_FIRM_ADMIN");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final LexMindProperties props;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository resetTokenRepository,
                       OrganizationRepository organizationRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService,
                       AuditService auditService, LexMindProperties props) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.props = props;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (!SELF_REGISTERABLE.contains(req.role())) {
            throw new BadRequestException("INVALID_ROLE", "Role not allowed for self-registration");
        }
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ConflictException("EMAIL_TAKEN", "Email is already registered");
        }
        Role role = roleRepository.findByNameWithPermissions(req.role())
                .orElseThrow(() -> new BadRequestException("INVALID_ROLE", "Unknown role"));

        User user = new User();
        user.setEmail(req.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setFullName(req.fullName());
        user.setRole(role);

        if ("LAW_FIRM_ADMIN".equals(req.role())
                && req.organizationName() != null && !req.organizationName().isBlank()) {
            Organization org = new Organization();
            org.setName(req.organizationName());
            org.setType("FIRM");
            org.setPlan("FIRM");
            org.setSeatLimit(10);
            organizationRepository.save(org);
            user.setOrganizationId(org.getId());
        }

        userRepository.save(user);
        UserPrincipal principal = CustomUserDetailsService.toPrincipal(user);
        auditService.record(principal, "USER_REGISTERED", "USER", user.getId(), null);
        return issueTokens(principal, user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = userRepository.findByEmailWithRole(req.email())
                .orElseThrow(() -> new UnauthorizedException("INVALID_CREDENTIALS", "Invalid credentials"));
        user.setLastLoginAt(Instant.now());
        UserPrincipal principal = CustomUserDetailsService.toPrincipal(user);
        auditService.record(principal, "USER_LOGIN", "USER", user.getId(), null);
        return issueTokens(principal, user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("INVALID_REFRESH", "Invalid refresh token"));
        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("INVALID_REFRESH", "Refresh token expired or revoked");
        }
        User user = userRepository.findByIdWithRole(token.getUserId())
                .orElseThrow(() -> new UnauthorizedException("INVALID_REFRESH", "User no longer exists"));
        token.setRevoked(true);   // rotate: single-use refresh tokens
        return issueTokens(CustomUserDetailsService.toPrincipal(user), user);
    }

    @Transactional
    public void forgotPassword(String email) {
        // Always behaves the same regardless of whether the email exists (no user enumeration).
        userRepository.findByEmailWithRole(email).ifPresent(user -> {
            String raw = generateOpaqueToken();
            PasswordResetToken prt = new PasswordResetToken();
            prt.setUserId(user.getId());
            prt.setTokenHash(sha256(raw));
            prt.setExpiresAt(Instant.now().plusSeconds(3600));
            resetTokenRepository.save(prt);
            // TODO Phase 9: send via SMTP. For now, log the reset link token (dev only).
            log.info("[password-reset] token for {} = {}", email, raw);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken prt = resetTokenRepository.findByTokenHash(sha256(rawToken))
                .orElseThrow(() -> new BadRequestException("INVALID_TOKEN", "Invalid or expired token"));
        if (prt.isUsed() || prt.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("INVALID_TOKEN", "Invalid or expired token");
        }
        User user = userRepository.findByIdWithRole(prt.getUserId())
                .orElseThrow(() -> new BadRequestException("INVALID_TOKEN", "Invalid token"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        prt.setUsed(true);
        refreshTokenRepository.revokeAllForUser(user.getId());
        auditService.record(CustomUserDetailsService.toPrincipal(user),
                "PASSWORD_RESET", "USER", user.getId(), null);
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(UUID userId) {
        return userRepository.findByIdWithRole(userId)
                .map(UserDto::from)
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not found"));
    }

    // ---- helpers ----

    private AuthResponse issueTokens(UserPrincipal principal, User user) {
        String access = jwtService.generateAccessToken(principal);
        String refreshRaw = generateOpaqueToken();
        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setTokenHash(sha256(refreshRaw));
        rt.setExpiresAt(Instant.now().plusSeconds(props.jwt().refreshTokenTtlSeconds()));
        refreshTokenRepository.save(rt);
        return new AuthResponse(access, refreshRaw, jwtService.getAccessTtlSeconds(), UserDto.from(user));
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
