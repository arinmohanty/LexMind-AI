import { Link } from "react-router-dom";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export function NotFoundPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 text-center">
      <h1 className="font-serif text-4xl font-semibold">404</h1>
      <p className="text-muted-foreground">This page could not be found.</p>
      <Link to="/" className={cn(buttonVariants())}>
        Back home
      </Link>
    </div>
  );
}
