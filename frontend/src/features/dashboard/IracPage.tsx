import { useParams } from "react-router-dom";
import { AiDisclaimer } from "@/components/domain";
import { useCase } from "@/features/cases/hooks";
import { IracView } from "@/features/dashboard/tabs";

export function IracPage() {
  const { caseId = "" } = useParams();
  const { data } = useCase(caseId);
  return (
    <div className="space-y-4">
      <div>
        <h1 className="font-serif text-2xl font-semibold">IRAC — {data?.title ?? "Case"}</h1>
        <p className="text-sm text-muted-foreground">
          Issue · Rule · Application · Conclusion
        </p>
      </div>
      <AiDisclaimer />
      <IracView caseId={caseId} />
    </div>
  );
}
