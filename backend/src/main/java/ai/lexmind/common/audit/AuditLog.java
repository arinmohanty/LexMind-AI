package ai.lexmind.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/** Append-only audit record (maps to {@code audit_logs}). */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "jsonb")
    private String metadataJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    // --- getters/setters ---
    public UUID getId() { return id; }
    public void setActorUserId(UUID v) { this.actorUserId = v; }
    public void setOrganizationId(UUID v) { this.organizationId = v; }
    public void setAction(String v) { this.action = v; }
    public void setResourceType(String v) { this.resourceType = v; }
    public void setResourceId(UUID v) { this.resourceId = v; }
    public void setIpAddress(String v) { this.ipAddress = v; }
    public void setUserAgent(String v) { this.userAgent = v; }
    public void setMetadataJson(String v) { this.metadataJson = v; }
    public Instant getCreatedAt() { return createdAt; }
}
