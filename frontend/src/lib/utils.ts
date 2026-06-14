import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

/** Tailwind-aware className combiner (ShadCN convention). */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/** Extracts a human message from an unknown error (ApiError, Error, or anything). */
export function getErrorMessage(err: unknown): string {
  if (typeof err === "object" && err !== null && "message" in err) {
    const m = (err as Record<string, unknown>).message;
    if (typeof m === "string" && m.length > 0) return m;
  }
  return "Something went wrong";
}

export function formatDate(value?: string | null): string {
  if (!value) return "—";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" });
}

export function relativeTime(value?: string | null): string {
  if (!value) return "—";
  const d = new Date(value).getTime();
  const diff = Date.now() - d;
  const mins = Math.round(diff / 60000);
  if (mins < 1) return "just now";
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.round(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return `${Math.round(hrs / 24)}d ago`;
}
