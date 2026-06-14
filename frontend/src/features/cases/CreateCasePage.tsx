import { useMutation } from "@tanstack/react-query";
import { FileUp, Sparkles } from "lucide-react";
import { useRef, useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { Spinner } from "@/components/domain";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { analysisApi } from "@/features/analysis/api";
import { useCaseDocuments, useCreateCase, useUploadDocument } from "@/features/cases/hooks";
import { getErrorMessage } from "@/lib/utils";
import type { CaseDetail, DocumentDto } from "@/types/api";

const CASE_TYPES = [
  "CRIMINAL", "CIVIL", "CONSTITUTIONAL", "CONTRACT", "FAMILY",
  "LABOUR", "CONSUMER", "COMPANY", "PROPERTY", "TAX", "OTHER",
];

function statusColor(status: DocumentDto["status"]): string {
  if (status === "DONE") return "text-success";
  if (status === "FAILED") return "text-danger";
  return "text-info";
}

export function CreateCasePage() {
  const navigate = useNavigate();
  const fileInput = useRef<HTMLInputElement>(null);

  const [created, setCreated] = useState<CaseDetail | null>(null);
  const [title, setTitle] = useState("");
  const [caseType, setCaseType] = useState("CRIMINAL");
  const [court, setCourt] = useState("");
  const [caseNumber, setCaseNumber] = useState("");

  const createCase = useCreateCase();
  const upload = useUploadDocument(created?.id ?? "");
  const docsQuery = useCaseDocuments(
    created?.id ?? "",
    created ? 3000 : undefined, // poll while a case exists and docs may be processing
  );

  const analyze = useMutation({
    mutationFn: () => analysisApi.start(created!.id),
    onSuccess: (run) => navigate(`/app/cases/${created!.id}/analysis?run=${run.id}`),
    onError: (err) => toast.error(getErrorMessage(err)),
  });

  const onCreate = (e: FormEvent) => {
    e.preventDefault();
    createCase.mutate(
      { title, caseType, court: court || undefined, caseNumber: caseNumber || undefined },
      {
        onSuccess: (c) => setCreated(c),
        onError: (err) => toast.error(getErrorMessage(err)),
      },
    );
  };

  const onFiles = async (files: FileList | null) => {
    if (!files) return;
    for (const file of Array.from(files)) {
      try {
        await upload.mutateAsync(file);
      } catch (err) {
        toast.error(`Upload failed: ${getErrorMessage(err)}`);
      }
    }
  };

  const docs = docsQuery.data ?? [];

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <h1 className="font-serif text-2xl font-semibold">New Case</h1>

      {/* Step 1: create the case */}
      <Card>
        <CardHeader>
          <CardTitle>1. Case details</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={onCreate} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="title">Title *</Label>
              <Input id="title" required value={title} disabled={!!created}
                onChange={(e) => setTitle(e.target.value)} placeholder="e.g. Sharma v. State" />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="type">Type</Label>
                <select id="type" value={caseType} disabled={!!created}
                  onChange={(e) => setCaseType(e.target.value)}
                  className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
                  {CASE_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                </select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="caseNumber">Case No.</Label>
                <Input id="caseNumber" value={caseNumber} disabled={!!created}
                  onChange={(e) => setCaseNumber(e.target.value)} />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="court">Court</Label>
              <Input id="court" value={court} disabled={!!created}
                onChange={(e) => setCourt(e.target.value)} />
            </div>
            {!created && (
              <Button type="submit" disabled={createCase.isPending}>
                {createCase.isPending && <Spinner />} Create case
              </Button>
            )}
          </form>
        </CardContent>
      </Card>

      {/* Step 2: upload documents + analyze */}
      {created && (
        <Card>
          <CardHeader>
            <CardTitle>2. Upload documents</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <button
              type="button"
              onClick={() => fileInput.current?.click()}
              className="flex w-full flex-col items-center gap-2 rounded-lg border border-dashed py-10 text-sm text-muted-foreground hover:border-brand hover:text-foreground"
            >
              <FileUp className="size-6" />
              Click to add FIR, judgment, petition, contract… (PDF · DOCX · images)
            </button>
            <input
              ref={fileInput}
              type="file"
              multiple
              accept=".pdf,.docx,.png,.jpg,.jpeg,.tiff"
              className="hidden"
              onChange={(e) => onFiles(e.target.files)}
            />

            {upload.isPending && (
              <p className="flex items-center gap-2 text-sm text-muted-foreground">
                <Spinner /> Uploading…
              </p>
            )}

            {docs.length > 0 && (
              <ul className="space-y-1 text-sm">
                {docs.map((d) => (
                  <li key={d.id} className="flex items-center justify-between">
                    <span className="truncate">{d.originalFilename}</span>
                    <span className={statusColor(d.status)}>{d.status.toLowerCase()}</span>
                  </li>
                ))}
              </ul>
            )}

            <div className="flex justify-end pt-2">
              <Button
                onClick={() => analyze.mutate()}
                disabled={docs.length === 0 || analyze.isPending}
              >
                {analyze.isPending ? <Spinner /> : <Sparkles className="size-4" />} Analyze case
              </Button>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
