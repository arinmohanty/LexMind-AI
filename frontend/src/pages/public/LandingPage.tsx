import { ArrowRight, Scale } from "lucide-react";
import { Link } from "react-router-dom";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

const FEATURES = ["Fact Matrix", "IRAC", "Statutes", "Evidence", "Precedents", "Risk"];

export function LandingPage() {
  return (
    <div className="min-h-screen bg-background">
      <header className="mx-auto flex max-w-6xl items-center justify-between p-6">
        <div className="flex items-center gap-2">
          <Scale className="size-6 text-accent" />
          <span className="font-serif text-xl font-semibold">LexMind AI</span>
        </div>
        <div className="flex items-center gap-2">
          <Link to="/login" className={cn(buttonVariants({ variant: "ghost" }))}>
            Login
          </Link>
          <Link to="/register" className={cn(buttonVariants())}>
            Sign up
          </Link>
        </div>
      </header>

      <section className="mx-auto max-w-3xl px-6 py-20 text-center">
        <h1 className="font-serif text-4xl font-semibold leading-tight sm:text-5xl">
          Upload a case. Get a complete legal intelligence dashboard.
        </h1>
        <p className="mt-5 text-lg text-muted-foreground">
          AI-powered case analysis for law students, advocates, and researchers — facts,
          issues, statutes, arguments, precedents and IRAC, structured in minutes.
        </p>
        <div className="mt-8 flex items-center justify-center gap-3">
          <Link to="/register" className={cn(buttonVariants({ size: "lg" }))}>
            Start free <ArrowRight className="size-4" />
          </Link>
          <Link to="/login" className={cn(buttonVariants({ size: "lg", variant: "outline" }))}>
            Log in
          </Link>
        </div>
        <div className="mt-10 flex flex-wrap items-center justify-center gap-2">
          {FEATURES.map((f) => (
            <span key={f} className="rounded-full border bg-card px-3 py-1 text-sm text-muted-foreground">
              {f}
            </span>
          ))}
        </div>
        <p className="mt-12 text-xs text-muted-foreground">
          ⚠ LexMind provides legal analysis, not legal advice.
        </p>
      </section>
    </div>
  );
}
