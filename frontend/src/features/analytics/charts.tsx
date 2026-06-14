import {
  Bar,
  BarChart,
  Cell,
  Pie,
  PieChart,
  PolarAngleAxis,
  RadialBar,
  RadialBarChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { MatterReadiness } from "@/types/api";

function readinessColor(pct: number): string {
  if (pct >= 66) return "hsl(var(--success))";
  if (pct >= 40) return "hsl(var(--warning))";
  return "hsl(var(--danger))";
}

/** Radial gauge for a single 0..1 readiness score. */
export function ReadinessGauge({ value, label }: { value: number | null; label?: string }) {
  const pct = Math.round((value ?? 0) * 100);
  const data = [{ name: "readiness", value: pct, fill: readinessColor(pct) }];
  return (
    <div className="relative" style={{ height: 160 }}>
      <ResponsiveContainer width="100%" height="100%">
        <RadialBarChart
          innerRadius="72%"
          outerRadius="100%"
          data={data}
          startAngle={90}
          endAngle={-270}
        >
          <PolarAngleAxis type="number" domain={[0, 100]} tick={false} />
          <RadialBar background dataKey="value" cornerRadius={10} />
        </RadialBarChart>
      </ResponsiveContainer>
      <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
        <span className="font-serif text-3xl font-semibold">{value == null ? "—" : `${pct}%`}</span>
        {label && <span className="text-xs text-muted-foreground">{label}</span>}
      </div>
    </div>
  );
}

const RISK_COLORS: Record<string, string> = {
  PROCEDURAL: "hsl(var(--info))",
  EVIDENTIARY: "hsl(var(--danger))",
  JURISDICTION: "hsl(var(--brand))",
  DOCUMENTATION: "hsl(var(--warning))",
};

export function RiskDonut({ data }: { data: Record<string, number> }) {
  const entries = Object.entries(data).map(([name, value]) => ({ name, value }));
  if (entries.length === 0) {
    return <p className="py-10 text-center text-sm text-muted-foreground">No risks recorded.</p>;
  }
  return (
    <ResponsiveContainer width="100%" height={220}>
      <PieChart>
        <Pie data={entries} dataKey="value" nameKey="name" innerRadius={55} outerRadius={85} paddingAngle={2}>
          {entries.map((e) => (
            <Cell key={e.name} fill={RISK_COLORS[e.name] ?? "hsl(var(--muted-foreground))"} />
          ))}
        </Pie>
        <Tooltip />
      </PieChart>
    </ResponsiveContainer>
  );
}

export function ReadinessByMatterBar({ matters }: { matters: MatterReadiness[] }) {
  const data = matters
    .filter((m) => m.overallReadiness != null)
    .slice(0, 12)
    .map((m) => ({
      name: m.title.length > 18 ? `${m.title.slice(0, 18)}…` : m.title,
      readiness: Math.round((m.overallReadiness ?? 0) * 100),
    }));
  if (data.length === 0) {
    return <p className="py-10 text-center text-sm text-muted-foreground">No readiness data yet.</p>;
  }
  return (
    <ResponsiveContainer width="100%" height={260}>
      <BarChart data={data} layout="vertical" margin={{ left: 24 }}>
        <XAxis type="number" domain={[0, 100]} tickFormatter={(v) => `${v}%`} fontSize={11} />
        <YAxis type="category" dataKey="name" width={120} fontSize={11} />
        <Tooltip formatter={(v) => `${v}%`} />
        <Bar dataKey="readiness" radius={[0, 4, 4, 0]}>
          {data.map((d) => (
            <Cell key={d.name} fill={readinessColor(d.readiness)} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  );
}

/** Horizontal sub-score bars for the four readiness dimensions. */
export function SubScoreBars({ scores }: { scores: { label: string; value: number | null }[] }) {
  return (
    <div className="space-y-2">
      {scores.map((s) => {
        const pct = Math.round((s.value ?? 0) * 100);
        return (
          <div key={s.label} className="flex items-center gap-3 text-sm">
            <span className="w-24 shrink-0 text-muted-foreground">{s.label}</span>
            <div className="h-2 flex-1 overflow-hidden rounded-full bg-muted">
              <div className="h-full rounded-full" style={{ width: `${pct}%`, background: readinessColor(pct) }} />
            </div>
            <span className="w-10 text-right tabular-nums">{s.value == null ? "—" : `${pct}%`}</span>
          </div>
        );
      })}
    </div>
  );
}
