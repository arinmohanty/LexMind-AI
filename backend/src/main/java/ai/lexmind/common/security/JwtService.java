package ai.lexmind.common.security;

import ai.lexmind.common.config.LexMindProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Issues and validates stateless JWT access tokens (ADR-0006).
 * Permissions are embedded as a claim so authorization needs no per-request DB hit.
 */
@Service
public class JwtService {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_PERMISSIONS = "perms";
    private static final String CLAIM_ORG = "org";

    private final SecretKey key;
    private final long accessTtlSeconds;
    private final String issuer;

    public JwtService(LexMindProperties props) {
        this.key = Keys.hmacShaKeyFor(props.jwt().secret().getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = props.jwt().accessTokenTtlSeconds();
        this.issuer = props.jwt().issuer();
    }

    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        List<String> perms = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .toList();
        return Jwts.builder()
                .issuer(issuer)
                .subject(principal.getId().toString())
                .claim(CLAIM_EMAIL, principal.getEmail())
                .claim(CLAIM_ROLE, principal.getRole())
                .claim(CLAIM_PERMISSIONS, perms)
                .claim(CLAIM_ORG, principal.getOrganizationId() == null ? null
                        : principal.getOrganizationId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .signWith(key)
                .compact();
    }

    /** Parse + verify a token and reconstruct the principal from its claims. */
    @SuppressWarnings("unchecked")
    public UserPrincipal parse(String token) {
        Claims c = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        UUID id = UUID.fromString(c.getSubject());
        String org = c.get(CLAIM_ORG, String.class);
        List<String> perms = c.get(CLAIM_PERMISSIONS, List.class);
        return new UserPrincipal(
                id,
                c.get(CLAIM_EMAIL, String.class),
                null,
                org == null ? null : UUID.fromString(org),
                c.get(CLAIM_ROLE, String.class),
                true,
                perms
        );
    }

    public long getAccessTtlSeconds() { return accessTtlSeconds; }
}
