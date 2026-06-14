import type { Role, User } from "@/types/api";

/**
 * Client-side permission map mirroring the backend RBAC seed (V1__init.sql). Used only to
 * show/hide UI — the backend remains the real authority.
 */
const ROLE_PERMISSIONS: Record<Role, string[]> = {
  SUPER_ADMIN: [
    "case:create", "case:read", "case:update", "case:delete", "document:upload",
    "analysis:run", "evidence:analyze", "witness:analyze", "strategy:view", "irac:view",
    "research:citation", "analytics:firm", "user:manage", "audit:read", "ai:monitor",
    "system:configure",
  ],
  LAW_FIRM_ADMIN: [
    "case:create", "case:read", "case:update", "case:delete", "document:upload",
    "analysis:run", "evidence:analyze", "witness:analyze", "strategy:view", "irac:view",
    "research:citation", "analytics:firm", "user:manage", "audit:read",
  ],
  ADVOCATE: [
    "case:create", "case:read", "case:update", "case:delete", "document:upload",
    "analysis:run", "evidence:analyze", "witness:analyze", "strategy:view", "irac:view",
    "research:citation",
  ],
  RESEARCHER: [
    "case:create", "case:read", "case:update", "case:delete", "document:upload",
    "analysis:run", "irac:view", "research:citation",
  ],
  LAW_STUDENT: [
    "case:create", "case:read", "case:update", "case:delete", "document:upload",
    "analysis:run", "irac:view", "research:citation",
  ],
};

export function permissionsFor(role: Role): string[] {
  return ROLE_PERMISSIONS[role] ?? [];
}

export function hasPermission(user: User | null, permission: string): boolean {
  if (!user) return false;
  return permissionsFor(user.role).includes(permission);
}
