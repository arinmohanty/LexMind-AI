import {
  BarChart3,
  FileText,
  FolderOpenDot,
  GraduationCap,
  LayoutDashboard,
  ScrollText,
  Search,
  Settings,
  ShieldCheck,
  Users,
} from "lucide-react";
import type { ComponentType } from "react";
import type { Role } from "@/types/api";

export interface NavItem {
  to: string;
  label: string;
  icon: ComponentType<{ className?: string }>;
}

const DASHBOARD: NavItem = { to: "/app/dashboard", label: "Dashboard", icon: LayoutDashboard };
const CASES: NavItem = { to: "/app/cases", label: "Cases", icon: FolderOpenDot };
const SETTINGS: NavItem = { to: "/app/settings", label: "Settings", icon: Settings };

const NAV: Record<Role, NavItem[]> = {
  LAW_STUDENT: [
    DASHBOARD,
    CASES,
    { to: "/app/research", label: "Research", icon: Search },
    SETTINGS,
  ],
  ADVOCATE: [
    DASHBOARD,
    { to: "/app/cases", label: "Case Repository", icon: FolderOpenDot },
    { to: "/app/analytics", label: "Analytics", icon: BarChart3 },
    { to: "/app/research", label: "Research", icon: Search },
    SETTINGS,
  ],
  RESEARCHER: [
    DASHBOARD,
    CASES,
    { to: "/app/research", label: "Research", icon: Search },
    { to: "/app/notes", label: "Notes", icon: ScrollText },
    SETTINGS,
  ],
  LAW_FIRM_ADMIN: [
    DASHBOARD,
    { to: "/app/cases", label: "All Matters", icon: FolderOpenDot },
    { to: "/app/analytics", label: "Analytics", icon: BarChart3 },
    { to: "/app/admin/users", label: "Team", icon: Users },
    SETTINGS,
  ],
  SUPER_ADMIN: [
    { to: "/app/admin", label: "Admin", icon: LayoutDashboard },
    { to: "/app/admin/users", label: "Users", icon: Users },
    { to: "/app/admin/ai-monitoring", label: "AI Monitoring", icon: BarChart3 },
    { to: "/app/admin/documents", label: "Documents", icon: FileText },
    { to: "/app/admin/audit", label: "Audit Logs", icon: ShieldCheck },
    SETTINGS,
  ],
};

export function navItemsFor(role: Role): NavItem[] {
  return NAV[role] ?? [DASHBOARD, CASES, SETTINGS];
}

export const studentLearningIcon = GraduationCap;
