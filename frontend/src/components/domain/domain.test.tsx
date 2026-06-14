import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { AiDisclaimer, ConfidenceBadge, FactStatusPill, RunStatusBadge } from "@/components/domain";

describe("domain components", () => {
  it("FactStatusPill renders the status label", () => {
    render(<FactStatusPill status="DISPUTED" />);
    expect(screen.getByText("disputed")).toBeInTheDocument();
  });

  it("ConfidenceBadge maps a score to a band", () => {
    render(<ConfidenceBadge value={0.9} />);
    expect(screen.getByText(/conf High/)).toBeInTheDocument();
  });

  it("ConfidenceBadge renders nothing for null", () => {
    const { container } = render(<ConfidenceBadge value={null} />);
    expect(container).toBeEmptyDOMElement();
  });

  it("AiDisclaimer states it is not legal advice", () => {
    render(<AiDisclaimer />);
    expect(screen.getByText(/not legal advice/i)).toBeInTheDocument();
  });

  it("RunStatusBadge renders the run status", () => {
    render(<RunStatusBadge status="COMPLETED" />);
    expect(screen.getByText("completed")).toBeInTheDocument();
  });
});
