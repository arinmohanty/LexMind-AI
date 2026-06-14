import { useState } from "react";
import { ConfidenceBadge, EmptyState, FactStatusPill, StrengthMeter } from "@/components/domain";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import {
  useArguments,
  useFacts,
  useIrac,
  useIssues,
  useOverview,
  useTimeline,
} from "@/features/dashboard/hooks";
import { formatDate } from "@/lib/utils";

function Loading() {
  return (
    <div className="space-y-2">
      {[0, 1, 2].map((i) => (
        <Skeleton key={i} className="h-16 w-full" />
      ))}
    </div>
  );
}

export function OverviewTab({ caseId }: { caseId: string }) {
  const { data, isLoading } = useOverview(caseId);
  if (isLoading) return <Loading />;
  if (!data) return <EmptyState title="No analysis yet" description="Run an analysis to populate this case." />;
  const fc = data.factCounts;
  return (
    <div className="grid gap-4 md:grid-cols-2">
      <Card>
        <CardHeader><CardTitle>Case snapshot</CardTitle></CardHeader>
        <CardContent className="space-y-1 text-sm">
          <Row label="Court" value={data.court} />
          <Row label="Case No." value={data.caseNumber} />
          <Row label="Type" value={data.caseType} />
          <Row label="Stage" value={data.stage} />
          <Row label="Filed" value={formatDate(data.filingDate)} />
        </CardContent>
      </Card>
      <Card>
        <CardHeader><CardTitle>Parties</CardTitle></CardHeader>
        <CardContent className="space-y-1 text-sm">
          {data.parties.length === 0 ? (
            <p className="text-muted-foreground">No parties recorded.</p>
          ) : (
            data.parties.map((p, i) => (
              <Row key={i} label={p.side} value={`${p.name}${p.counsel ? ` (${p.counsel})` : ""}`} />
            ))
          )}
        </CardContent>
      </Card>
      <Card className="md:col-span-2">
        <CardHeader><CardTitle>Quick stats</CardTitle></CardHeader>
        <CardContent className="flex flex-wrap gap-3 text-sm">
          <Badge variant="success">established {fc.established}</Badge>
          <Badge variant="warning">disputed {fc.disputed}</Badge>
          <Badge variant="danger">missing {fc.missing}</Badge>
          <Badge variant="brand">issues {data.issueCount}</Badge>
          <Badge variant="brand">arguments {data.argumentCount}</Badge>
          <Badge variant="brand">timeline {data.timelineCount}</Badge>
        </CardContent>
      </Card>
    </div>
  );
}

function Row({ label, value }: { label: string; value?: string | null }) {
  return (
    <div className="flex justify-between gap-4">
      <span className="text-muted-foreground">{label}</span>
      <span className="text-right font-medium">{value || "—"}</span>
    </div>
  );
}

export function TimelineTab({ caseId }: { caseId: string }) {
  const { data, isLoading } = useTimeline(caseId);
  if (isLoading) return <Loading />;
  if (!data || data.length === 0) return <EmptyState title="No timeline yet" />;
  return (
    <ol className="relative space-y-4 border-l pl-6">
      {data.map((e) => (
        <li key={e.id} className="relative">
          <span className="absolute -left-[27px] top-1 size-3 rounded-full bg-info" />
          <div className="text-xs text-muted-foreground">
            {formatDate(e.eventDate)} {e.eventType ? `· ${e.eventType}` : ""}
          </div>
          <div className="text-sm">{e.eventText}</div>
        </li>
      ))}
    </ol>
  );
}

const FACT_FILTERS = ["ALL", "ESTABLISHED", "DISPUTED", "MISSING"] as const;

export function FactsTab({ caseId }: { caseId: string }) {
  const [filter, setFilter] = useState<(typeof FACT_FILTERS)[number]>("ALL");
  const { data, isLoading } = useFacts(caseId, filter === "ALL" ? undefined : filter);
  return (
    <div className="space-y-3">
      <div className="flex flex-wrap gap-2">
        {FACT_FILTERS.map((f) => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={`rounded-full border px-3 py-1 text-xs ${
              filter === f ? "border-brand bg-brand/10 text-brand" : "text-muted-foreground"
            }`}
          >
            {f.toLowerCase()}
          </button>
        ))}
      </div>
      {isLoading ? (
        <Loading />
      ) : !data || data.length === 0 ? (
        <EmptyState title="No facts" />
      ) : (
        <ul className="space-y-2">
          {data.map((f) => (
            <li key={f.id} className="flex items-start justify-between gap-3 rounded-md border p-3">
              <div className="space-y-1">
                <p className="text-sm">{f.factText}</p>
                {f.sourceExcerpt && (
                  <p className="font-mono text-xs text-muted-foreground">“{f.sourceExcerpt}”</p>
                )}
              </div>
              <div className="flex shrink-0 flex-col items-end gap-1">
                <FactStatusPill status={f.factStatus} />
                <ConfidenceBadge value={f.confidence} />
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export function IssuesTab({ caseId }: { caseId: string }) {
  const { data, isLoading } = useIssues(caseId);
  if (isLoading) return <Loading />;
  if (!data || data.length === 0) return <EmptyState title="No issues identified" />;
  return (
    <ul className="space-y-2">
      {data.map((i) => (
        <li key={i.id} className="flex items-center justify-between rounded-md border p-3">
          <div>
            <span className="mr-2 font-mono text-xs text-muted-foreground">#{i.rank}</span>
            <span className="text-sm">{i.issueText}</span>
          </div>
          <Badge variant={i.issueType === "PRIMARY" ? "brand" : "default"}>
            {i.issueType.toLowerCase()}
          </Badge>
        </li>
      ))}
    </ul>
  );
}

export function ArgumentsTab({ caseId }: { caseId: string }) {
  const { data, isLoading } = useArguments(caseId);
  if (isLoading) return <Loading />;
  if (!data || (data.petitioner.length === 0 && data.respondent.length === 0))
    return <EmptyState title="No arguments yet" />;
  return (
    <div className="grid gap-4 md:grid-cols-2">
      {(["petitioner", "respondent"] as const).map((side) => (
        <Card key={side}>
          <CardHeader><CardTitle className="capitalize">{side}</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            {data[side].length === 0 ? (
              <p className="text-sm text-muted-foreground">None.</p>
            ) : (
              data[side].map((a) => (
                <div key={a.id} className="space-y-1">
                  <StrengthMeter strength={a.strength} />
                  <p className="text-sm">{a.argumentText}</p>
                </div>
              ))
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

export function IracView({ caseId }: { caseId: string }) {
  const { data, isLoading } = useIrac(caseId);
  if (isLoading) return <Loading />;
  if (!data || data.length === 0) return <EmptyState title="No IRAC analysis yet" />;
  return (
    <div className="space-y-4">
      {data.map((ir) => (
        <Card key={ir.id}>
          <CardContent className="divide-y p-0">
            <IracRow label="Issue" text={ir.issue} />
            <IracRow label="Rule" text={ir.rule} />
            <IracRow label="Application" text={ir.application} />
            <IracRow label="Conclusion" text={ir.conclusion} />
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

function IracRow({ label, text }: { label: string; text: string }) {
  return (
    <div className="p-4">
      <div className="text-xs font-semibold uppercase tracking-wide text-brand">{label}</div>
      <p className="mt-1 text-sm">{text}</p>
    </div>
  );
}
