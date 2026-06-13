package ai.lexmind.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Authenticated principal. Carries identity + RBAC authorities so authorization is fully
 * stateless (built either from the DB at login or reconstructed from JWT claims per request).
 */
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String passwordHash;     // null when reconstructed from a JWT
    private final UUID organizationId;
    private final String role;             // e.g. ADVOCATE
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID id, String email, String passwordHash, UUID organizationId,
                         String role, boolean active, List<String> permissionCodes) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.organizationId = organizationId;
        this.role = role;
        this.active = active;
        List<GrantedAuthority> auths = new ArrayList<>();
        auths.add(new SimpleGrantedAuthority("ROLE_" + role));
        if (permissionCodes != null) {
            permissionCodes.forEach(p -> auths.add(new SimpleGrantedAuthority(p)));
        }
        this.authorities = auths;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public UUID getOrganizationId() { return organizationId; }
    public String getRole() { return role; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return active; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return active; }
}
