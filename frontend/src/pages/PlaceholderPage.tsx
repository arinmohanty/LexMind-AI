import { Construction } from "lucide-react";
import { EmptyState } from "@/components/domain";

export function PlaceholderPage({ title }: { title: string }) {
  return (
    <div className="space-y-4">
      <h1 className="font-serif text-2xl font-semibold">{title}</h1>
      <EmptyState
        icon={<Construction className="size-8" />}
        title={`${title} is coming soon`}
        description="This area is part of a later build phase (v1/v2)."
      />
    </div>
  );
}
