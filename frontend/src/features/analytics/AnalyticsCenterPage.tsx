import { Link } from "react-router-dom";
import { EmptyState } from "@/components/domain";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ReadinessByMatterBar, RiskDonut } from "@/features/analytics/charts";
import { usePortfolio } from "@/features/analytics/hooks";

function Kpi({ label, value }: { label: string; value: string }) {
  return (
    <Card>
      <CardContent className="p-5">
        <p className="text-sm text-muted-foreground">{label}</p>
        <p className="mt-1 font-serif text-3xl font-semibold">{value}</p>
      </CardContent>
    </Card>
  );
}

export function AnalyticsCenterPage() {
  const { data, isLoading } = usePortfolio();

  if (isLoading) return <Skeleton className="h-72 w-full" />;
  if (!data || data.caseCount === 0) {
    return (
      <div className="space-y-4">
        <h1 className="font-serif text-2xl font-semibold">Analytics Center</h1>
        <EmptyState title="No analyzed cases yet" description="Analytics appear once you run analysis on your cases." />
      </div>
    );
  }

  const avg = data.avgReadiness == null ? "—" : `${Math.round(data.avgReadiness * 100)}%`;

  return (
    <div className="space-y-6">
      <h1 className="font-serif text-2xl font-semibold">Analytics Center</h1>

      <div className="grid gap-4 sm:grid-cols-3">
        <Kpi label="Cases" value={String(data.caseCount)} />
        <Kpi label="Avg readiness" value={avg} />
        <Kpi label="High-risk cases" value={String(data.highRiskCases)} />
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Readiness by matter</CardTitle>
          </CardHeader>
          <CardContent>
            <ReadinessByMatterBar matters={data.matters} />
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Risk by type</CardTitle>
          </CardHeader>
          <CardContent>
            <RiskDonut data={data.riskByType} />
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Matters</CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="divide-y text-sm">
            {data.matters.map((m) => (
              <li key={m.caseId} className="flex items-center justify-between py-2">
                <Link to={`/app/cases/${m.caseId}/analysis?tab=analytics`} className="hover:text-brand hover:underline">
                  {m.title}
                </Link>
                <span className="text-muted-foreground">
                  {m.overallReadiness == null ? "—" : `${Math.round(m.overallReadiness * 100)}%`}
                  {m.highRisks > 0 && <span className="ml-2 text-danger">{m.highRisks} high-risk</span>}
                </span>
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  );
}
