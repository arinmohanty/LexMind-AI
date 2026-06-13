package ai.lexmind.common.security;

import ai.lexmind.common.config.LexMindProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates server-to-server callbacks from the AI service on {@code /internal/**}
 * using a shared service token (ADR-0002). Not exposed to end users.
 */
@Component
public class InternalServiceTokenFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Internal-Token";
    private final String expectedToken;

    public InternalServiceTokenFilter(LexMindProperties props) {
        this.expectedToken = props.ai().serviceToken();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String token = request.getHeader(HEADER);
        if (expectedToken != null && expectedToken.equals(token)) {
            var auth = new UsernamePasswordAuthenticationToken(
                    "ai-service", null, List.of(new SimpleGrantedAuthority("ROLE_INTERNAL")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }
}
