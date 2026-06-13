package ai.lexmind.common.audit;

import ai.lexmind.common.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/** Persists audit records. Writes are async so auditing never slows the request path. */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Async
    public void record(UserPrincipal actor, String action, String resourceType,
                       UUID resourceId, String metadataJson) {
        try {
            AuditLog entry = new AuditLog();
            if (actor != null) {
                entry.setActorUserId(actor.getId());
                entry.setOrganizationId(actor.getOrganizationId());
            }
            entry.setAction(action);
            entry.setResourceType(resourceType);
            entry.setResourceId(resourceId);
            entry.setMetadataJson(metadataJson);
            captureRequestContext(entry);
            repository.save(entry);
        } catch (Exception ex) {
            log.warn("Failed to write audit log for action {} {}", action, resourceType, ex);
        }
    }

    private void captureRequestContext(AuditLog entry) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            var req = attrs.getRequest();
            entry.setIpAddress(req.getRemoteAddr());
            entry.setUserAgent(req.getHeader("User-Agent"));
        }
    }
}
