package ai.lexmind.common.audit;

import ai.lexmind.common.security.UserPrincipal;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Records an audit entry after any successful {@link Audited} method (AOP, LLD §1).
 * Fine-grained audits (with resource ids/metadata) call {@link AuditService} directly.
 */
@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @AfterReturning("@annotation(audited)")
    public void afterAudited(Audited audited) {
        UserPrincipal actor = null;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal up) {
            actor = up;
        }
        auditService.record(actor, audited.action(), audited.resourceType(), null, null);
    }
}
