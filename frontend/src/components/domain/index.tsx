import { AlertTriangle, Loader2 } from "lucide-react";
import type { ReactNode } from "react";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import type { FactStatus, RunStatus } from "@/types/api";

/** Persistent "analysis, not legal advice" cue (design system §3). */
export function AiDisclaimer({ className }: { className?: string }) {
  return (
    <div
      className={cn(
        "flex items-center gap-2 rounded-md border border-warning/30 bg-warning/10 px-3 py-1.5 text-xs text-[hsl(var(--warning))]",
        className,
      )}
    >
      <AlertTriangle className="size-3.5 shrink-0" />
      <span>LexMind provides legal analysis, not legal advice.</span>
    </div>
  );
}

const FACT_VARIANT: Record<FactStatus, "success" | "warning" | "danger"> = {
  ESTABLISHED: "success",
  DISPUTED: "warning",
  MISSING: "danger",
};

export function FactStatusPill({ status }: { status: FactStatus }) {
  return <Badge variant={FACT_VARIANT[status]}>{status.toLowerCase()}</Badge>;
}

export function ConfidenceBadge({ value }: { value: number | null }) {
  if (value == null) return null;
  const label = value >= 0.75 ? "High" : value >= 0.5 ? "Med" : "Low";
  const variant = value >= 0.75 ? "success" : value >= 0.5 ? "warning" : "danger";
  return <Badge variant={variant}>conf {label}</Badge>;
}

const STRENGTH_FILL: Record<string, number> = { STRONG: 4, MODERATE: 3, WEAK: 2 };

export function StrengthMeter({ strength }: { strength: string | null }) {
  const fill = strength ? STRENGTH_FILL[strength] ?? 0 : 0;
  const color =
    strength === "STRONG" ? "bg-success" : strength === "MODERATE" ? "bg-warning" : "bg-danger";
  return (
    <span className="inline-flex items-center gap-1" title={strength ?? "—"}>
      {[1, 2, 3, 4].map((i) => (
        <span
          key={i}
          className={cn("h-1.5 w-4 rounded-sm", i <= fill ? color : "bg-muted")}
        />
      ))}
    </span>
  );
}

const RUN_VARIANT: Record<RunStatus, "default" | "info" | "success" | "danger" | "warning"> = {
  QUEUED: "default",
  PROCESSING: "info",
  COMPLETED: "success",
  FAILED: "danger",
  PARTIAL: "warning",
};

export function RunStatusBadge({ status }: { status: RunStatus }) {
  return <Badge variant={RUN_VARIANT[status]}>{status.toLowerCase()}</Badge>;
}

export function Spinner({ className }: { className?: string }) {
  return <Loader2 className={cn("size-4 animate-spin", className)} />;
}

export function EmptyState({
  icon,
  title,
  description,
  action,
}: {
  icon?: ReactNode;
  title: string;
  description?: string;
  action?: ReactNode;
}) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-lg border border-dashed py-16 text-center">
      {icon && <div className="text-muted-foreground">{icon}</div>}
      <div>
        <p className="font-medium">{title}</p>
        {description && <p className="mt-1 text-sm text-muted-foreground">{description}</p>}
      </div>
      {action}
    </div>
  );
}
