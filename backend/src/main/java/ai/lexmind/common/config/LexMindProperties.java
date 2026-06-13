package ai.lexmind.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Strongly-typed binding of all {@code lexmind.*} configuration (12-factor / ADR conventions).
 */
@ConfigurationProperties(prefix = "lexmind")
public record LexMindProperties(
        Jwt jwt,
        Storage storage,
        Ai ai,
        Cors cors
) {
    public record Jwt(
            String secret,
            long accessTokenTtlSeconds,
            long refreshTokenTtlSeconds,
            String issuer
    ) {}

    public record Storage(
            String backend,
            String localPath,
            long maxFileBytes,
            List<String> allowedMimeTypes
    ) {}

    public record Ai(
            String baseUrl,
            String serviceToken,
            int timeoutSeconds
    ) {}

    public record Cors(
            List<String> allowedOrigins
    ) {}
}
