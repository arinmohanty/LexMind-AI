import { createContext, useContext, type ReactNode } from "react";
import { cn } from "@/lib/utils";

interface TabsCtx {
  value: string;
  onValueChange: (v: string) => void;
}
const Ctx = createContext<TabsCtx | null>(null);

export function Tabs({
  value,
  onValueChange,
  children,
  className,
}: TabsCtx & { children: ReactNode; className?: string }) {
  return (
    <Ctx.Provider value={{ value, onValueChange }}>
      <div className={className}>{children}</div>
    </Ctx.Provider>
  );
}

export function TabsList({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <div
      role="tablist"
      className={cn(
        "flex flex-wrap items-center gap-1 overflow-x-auto border-b border-border",
        className,
      )}
    >
      {children}
    </div>
  );
}

export function TabsTrigger({ value, children }: { value: string; children: ReactNode }) {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error("TabsTrigger must be used within Tabs");
  const active = ctx.value === value;
  return (
    <button
      role="tab"
      aria-selected={active}
      onClick={() => ctx.onValueChange(value)}
      className={cn(
        "-mb-px whitespace-nowrap border-b-2 px-3 py-2 text-sm font-medium transition-colors",
        active
          ? "border-brand text-brand"
          : "border-transparent text-muted-foreground hover:text-foreground",
      )}
    >
      {children}
    </button>
  );
}

export function TabsContent({
  value,
  children,
  className,
}: {
  value: string;
  children: ReactNode;
  className?: string;
}) {
  const ctx = useContext(Ctx);
  if (!ctx || ctx.value !== value) return null;
  return <div className={cn("pt-4", className)}>{children}</div>;
}
