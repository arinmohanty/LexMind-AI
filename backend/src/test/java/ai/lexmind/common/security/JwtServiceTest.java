package ai.lexmind.common.security;

import ai.lexmind.common.config.LexMindProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        LexMindProperties props = new LexMindProperties(
                new LexMindProperties.Jwt(
                        "a-very-long-test-secret-key-at-least-32-bytes-long!!", 900, 1209600, "lexmind-ai"),
                null, null, null);
        jwtService = new JwtService(props);
    }

    @Test
    void generatesAndParsesTokenPreservingIdentityAndAuthorities() {
        UUID id = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(
                id, "adv@example.com", null, null, "ADVOCATE", true,
                List.of("case:read", "case:create"));

        String token = jwtService.generateAccessToken(principal);
        UserPrincipal parsed = jwtService.parse(token);

        assertThat(parsed.getId()).isEqualTo(id);
        assertThat(parsed.getEmail()).isEqualTo("adv@example.com");
        assertThat(parsed.getRole()).isEqualTo("ADVOCATE");
        assertThat(parsed.getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADVOCATE", "case:read", "case:create");
    }

    @Test
    void rejectsTamperedToken() {
        assertThatThrownBy(() -> jwtService.parse("not.a.valid.token"))
                .isInstanceOf(Exception.class);
    }
}
