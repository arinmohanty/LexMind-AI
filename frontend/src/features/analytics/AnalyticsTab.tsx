import { EmptyState } from "@/components/domain";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ReadinessGauge, SubScoreBars } from "@/features/analytics/charts";
import { useCaseAnalytics } from "@/features/analytics/hooks";
import type { Risk } from "@/types/api";

function severityVariant(sev: Risk["severity"]) {
  if (sev === "HIGH") return "danger" as const;
  if (sev === "MEDIUM") return "warning" as const;
  return "info" as const;
}

function StrengthList({ title, items }: { title: string; items: string[] }) {
  if (!items?.length) return null;
  return (
    <div>
      <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">{title}</p>
      <ul className="mt-1 list-inside list-disc text-sm">
        {items.map((i, idx) => (
          <li key={idx}>{i}</li>
        ))}
      </ul>
    </div>
  );
}

export function AnalyticsTab({ caseId }: { caseId: string }) {
  const { data, isLoading } = useCaseAnalytics(caseId);

  if (isLoading) return <Skeleton className="h-64 w-full" />;
  if (!data || (!data.readiness && !data.strength && data.risks.length === 0)) {
    return <EmptyState title="No analytics yet" description="Run an analysis to compute strength, risk, and readiness." />;
  }

  const r = data.readiness;
  const s = data.strength;

  return (
    <div className="grid gap-4 md:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle>Litigation readiness</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <ReadinessGauge value={r?.overallReadiness ?? null} label="overall" />
          <SubScoreBars
            scores={[
              { label: "Evidence", value: r?.evidenceReadiness ?? null },
              { label: "Witness", value: r?.witnessReadiness ?? null },
              { label: "Research", value: r?.researchReadiness ?? null },
              { label: "Hearing", value: r?.hearingReadiness ?? null },
            ]}
          />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Case strength</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {s?.overallScore != null && (
            <div className="text-sm">
              Overall score:{" "}
              <span className="font-semibold">{Math.round(s.overallScore * 100)}%</span>
            </div>
          )}
          <StrengthList title="Strong" items={s?.strong ?? []} />
          <StrengthList title="Weak" items={s?.weak ?? []} />
          <StrengthList title="Missing evidence" items={s?.missingEvidence ?? []} />
          {!s && <p className="text-sm text-muted-foreground">No strength findings.</p>}
        </CardContent>
      </Card>

      <Card className="md:col-span-2">
        <CardHeader>
          <CardTitle>Risk analysis</CardTitle>
        </CardHeader>
        <CardContent>
          {data.risks.length === 0 ? (
            <p className="text-sm text-muted-foreground">No risks flagged.</p>
          ) : (
            <ul className="space-y-2">
              {data.risks.map((risk) => (
                <li key={risk.id} className="flex items-start justify-between gap-3 rounded-md border p-3">
                  <div>
                    <Badge variant="default">{risk.riskType.toLowerCase()}</Badge>
                    <p className="mt-1 text-sm">{risk.description}</p>
                  </div>
                  {risk.severity && (
                    <Badge variant={severityVariant(risk.severity)}>{risk.severity.toLowerCase()}</Badge>
                  )}
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
