import { describe, expect, it } from "vitest";
import { hasPermission, permissionsFor } from "@/lib/rbac";
import type { Role, User } from "@/types/api";

function user(role: Role): User {
  return {
    id: "1", email: "a@b.com", fullName: "Test", role,
    organizationId: null, status: "ACTIVE", emailVerified: true,
  };
}

describe("rbac", () => {
  it("grants admin every permission", () => {
    expect(hasPermission(user("SUPER_ADMIN"), "system:configure")).toBe(true);
    expect(hasPermission(user("SUPER_ADMIN"), "ai:monitor")).toBe(true);
  });

  it("limits advocate to practice permissions", () => {
    expect(hasPermission(user("ADVOCATE"), "evidence:analyze")).toBe(true);
    expect(hasPermission(user("ADVOCATE"), "user:manage")).toBe(false);
  });

  it("limits students (no evidence analysis)", () => {
    expect(hasPermission(user("LAW_STUDENT"), "irac:view")).toBe(true);
    expect(hasPermission(user("LAW_STUDENT"), "evidence:analyze")).toBe(false);
  });

  it("returns false for null user", () => {
    expect(hasPermission(null, "case:read")).toBe(false);
  });

  it("exposes the permission list per role", () => {
    expect(permissionsFor("RESEARCHER")).toContain("research:citation");
  });
});
