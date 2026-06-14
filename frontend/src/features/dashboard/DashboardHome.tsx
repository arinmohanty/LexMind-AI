import { FileText, Upload } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { EmptyState, Spinner } from "@/components/domain";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useAuth } from "@/features/auth/AuthProvider";
import { useCases } from "@/features/cases/hooks";
import { relativeTime } from "@/lib/utils";

export function DashboardHome() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { data, isLoading } = useCases(0);

  const cases = data?.content ?? [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-serif text-2xl font-semibold">
            Welcome, {user?.fullName?.split(" ")[0] ?? "there"}
          </h1>
          <p className="text-sm text-muted-foreground">
            Your legal intelligence workspace.
          </p>
        </div>
        <Button onClick={() => navigate("/app/cases/new")}>
          <Upload className="size-4" /> Upload Case
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Recent cases</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Spinner /> Loading…
            </div>
          ) : cases.length === 0 ? (
            <EmptyState
              icon={<FileText className="size-8" />}
              title="No cases yet"
              description="Upload your first case to generate a legal intelligence dashboard."
              action={
                <Button onClick={() => navigate("/app/cases/new")}>
                  <Upload className="size-4" /> Upload your first case
                </Button>
              }
            />
          ) : (
            <ul className="divide-y">
              {cases.slice(0, 6).map((c) => (
                <li key={c.id} className="flex items-center justify-between py-3">
                  <Link
                    to={`/app/cases/${c.id}/analysis`}
                    className="font-medium hover:text-brand hover:underline"
                  >
                    {c.title}
                  </Link>
                  <span className="text-xs text-muted-foreground">
                    {c.stage ?? "—"} · {relativeTime(c.updatedAt)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
