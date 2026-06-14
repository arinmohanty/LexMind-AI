import { describe, expect, it } from "vitest";
import { cn, formatDate, getErrorMessage, relativeTime } from "@/lib/utils";

describe("cn", () => {
  it("merges and dedupes tailwind classes", () => {
    expect(cn("p-2", "p-4")).toBe("p-4");
    expect(cn("text-sm", false && "hidden", "font-bold")).toBe("text-sm font-bold");
  });
});

describe("getErrorMessage", () => {
  it("reads message from an ApiError-shaped object", () => {
    expect(getErrorMessage({ code: "X", message: "boom" })).toBe("boom");
  });
  it("reads message from an Error", () => {
    expect(getErrorMessage(new Error("nope"))).toBe("nope");
  });
  it("falls back for unknown shapes", () => {
    expect(getErrorMessage(42)).toBe("Something went wrong");
    expect(getErrorMessage(null)).toBe("Something went wrong");
  });
});

describe("formatDate", () => {
  it("returns dash for empty", () => {
    expect(formatDate(null)).toBe("—");
  });
  it("formats an ISO date", () => {
    expect(formatDate("2024-01-12")).toMatch(/2024/);
  });
});

describe("relativeTime", () => {
  it("returns dash for empty", () => {
    expect(relativeTime(undefined)).toBe("—");
  });
  it("formats recent times", () => {
    const now = new Date().toISOString();
    expect(relativeTime(now)).toMatch(/just now|m ago/);
  });
});
