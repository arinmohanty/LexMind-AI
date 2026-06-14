import { FolderPlus } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { EmptyState } from "@/components/domain";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useCases } from "@/features/cases/hooks";
import { relativeTime } from "@/lib/utils";

export function CasesListPage() {
  const navigate = useNavigate();
  const { data, isLoading } = useCases(0);
  const cases = data?.content ?? [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-serif text-2xl font-semibold">Case Repository</h1>
          <p className="text-sm text-muted-foreground">All matters you can access.</p>
        </div>
        <Button onClick={() => navigate("/app/cases/new")}>
          <FolderPlus className="size-4" /> New Case
        </Button>
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {[0, 1, 2, 3].map((i) => (
            <Skeleton key={i} className="h-12 w-full" />
          ))}
        </div>
      ) : cases.length === 0 ? (
        <EmptyState
          title="No cases yet"
          description="Create a case and upload documents to get started."
          action={
            <Button onClick={() => navigate("/app/cases/new")}>
              <FolderPlus className="size-4" /> New Case
            </Button>
          }
        />
      ) : (
        <Card className="overflow-hidden">
          <table className="w-full text-sm">
            <thead className="border-b bg-muted/50 text-left text-xs uppercase text-muted-foreground">
              <tr>
                <th className="px-4 py-3 font-medium">Title</th>
                <th className="px-4 py-3 font-medium">Case No.</th>
                <th className="px-4 py-3 font-medium">Court</th>
                <th className="px-4 py-3 font-medium">Type</th>
                <th className="px-4 py-3 font-medium">Stage</th>
                <th className="px-4 py-3 font-medium">Updated</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {cases.map((c) => (
                <tr key={c.id} className="hover:bg-muted/40">
                  <td className="px-4 py-3">
                    <Link
                      to={`/app/cases/${c.id}/analysis`}
                      className="font-medium hover:text-brand hover:underline"
                    >
                      {c.title}
                    </Link>
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-muted-foreground">
                    {c.caseNumber ?? "—"}
                  </td>
                  <td className="px-4 py-3 text-muted-foreground">{c.court ?? "—"}</td>
                  <td className="px-4 py-3 text-muted-foreground">{c.caseType ?? "—"}</td>
                  <td className="px-4 py-3 text-muted-foreground">{c.stage ?? "—"}</td>
                  <td className="px-4 py-3 text-muted-foreground">{relativeTime(c.updatedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}
    </div>
  );
}
