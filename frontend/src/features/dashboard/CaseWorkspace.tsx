import { RefreshCw } from "lucide-react";
import { useParams, useSearchParams } from "react-router-dom";
import { toast } from "sonner";
import { AiDisclaimer, RunStatusBadge, Spinner } from "@/components/domain";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useAnalysisRun, useStartAnalysis } from "@/features/analysis/hooks";
import { useCase } from "@/features/cases/hooks";
import { AnalyticsTab } from "@/features/analytics/AnalyticsTab";
import {
  ArgumentsTab,
  FactsTab,
  IracView,
  IssuesTab,
  OverviewTab,
  TimelineTab,
} from "@/features/dashboard/tabs";
import { getErrorMessage } from "@/lib/utils";

const TABS = [
  ["overview", "Overview"],
  ["timeline", "Timeline"],
  ["facts", "Facts"],
  ["issues", "Issues"],
  ["arguments", "Arguments"],
  ["irac", "IRAC"],
  ["analytics", "Analytics"],
] as const;

export function CaseWorkspace() {
  const { caseId = "" } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const tab = searchParams.get("tab") ?? "overview";
  const runId = searchParams.get("run");

  const caseQuery = useCase(caseId);
  const runQuery = useAnalysisRun(runId, caseId);
  const startAnalysis = useStartAnalysis(caseId);

  const setTab = (value: string) => {
    const next = new URLSearchParams(searchParams);
    next.set("tab", value);
    setSearchParams(next, { replace: true });
  };

  const onReRun = () => {
    startAnalysis.mutate(undefined, {
      onSuccess: (run) => {
        const next = new URLSearchParams(searchParams);
        next.set("run", run.id);
        setSearchParams(next, { replace: true });
        toast.success("Analysis started");
      },
      onError: (err) => toast.error(getErrorMessage(err)),
    });
  };

  const c = caseQuery.data;
  const run = runQuery.data;
  const running = run && (run.status === "PROCESSING" || run.status === "QUEUED");

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="font-serif text-2xl font-semibold">{c?.title ?? "Case"}</h1>
          <p className="text-sm text-muted-foreground">
            {[c?.caseNumber, c?.court, c?.stage].filter(Boolean).join(" · ") || "—"}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={() => toast.info("Export arrives in a later phase")}>
            Export
          </Button>
          <Button size="sm" onClick={onReRun} disabled={startAnalysis.isPending}>
            {startAnalysis.isPending ? <Spinner /> : <RefreshCw className="size-4" />} Re-run
          </Button>
        </div>
      </div>

      <AiDisclaimer />

      {run && (
        <div className="flex flex-wrap items-center gap-2 rounded-md border bg-muted/40 p-3 text-sm">
          {running && <Spinner />}
          <span>Analysis</span>
          <RunStatusBadge status={run.status} />
          {run.agents.length > 0 && (
            <span className="text-xs text-muted-foreground">
              {run.agents.filter((a) => a.status === "COMPLETED").length}/{run.agents.length} agents done
            </span>
          )}
          {run.status === "FAILED" && run.errorMessage && (
            <span className="text-xs text-danger">— {run.errorMessage}</span>
          )}
        </div>
      )}

      <Tabs value={tab} onValueChange={setTab}>
        <TabsList>
          {TABS.map(([value, label]) => (
            <TabsTrigger key={value} value={value}>
              {label}
            </TabsTrigger>
          ))}
        </TabsList>
        <TabsContent value="overview"><OverviewTab caseId={caseId} /></TabsContent>
        <TabsContent value="timeline"><TimelineTab caseId={caseId} /></TabsContent>
        <TabsContent value="facts"><FactsTab caseId={caseId} /></TabsContent>
        <TabsContent value="issues"><IssuesTab caseId={caseId} /></TabsContent>
        <TabsContent value="arguments"><ArgumentsTab caseId={caseId} /></TabsContent>
        <TabsContent value="irac"><IracView caseId={caseId} /></TabsContent>
        <TabsContent value="analytics"><AnalyticsTab caseId={caseId} /></TabsContent>
      </Tabs>
    </div>
  );
}
