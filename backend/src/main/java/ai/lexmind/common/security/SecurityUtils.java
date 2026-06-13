package ai.lexmind.common.security;

import ai.lexmind.common.error.AppExceptions;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Convenience access to the authenticated {@link UserPrincipal}. */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserPrincipal currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal up)) {
            throw new AppExceptions.UnauthorizedException("UNAUTHORIZED", "No authenticated user");
        }
        return up;
    }
}
