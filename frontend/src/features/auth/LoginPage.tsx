import { useMutation } from "@tanstack/react-query";
import { Scale } from "lucide-react";
import { useState, type FormEvent } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Spinner } from "@/components/domain";
import { authApi } from "@/features/auth/api";
import { useAuth } from "@/features/auth/AuthProvider";
import { getErrorMessage } from "@/lib/utils";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string })?.from ?? "/app/dashboard";

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const mutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: (auth) => {
      login(auth);
      navigate(from, { replace: true });
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  });

  const onSubmit = (e: FormEvent) => {
    e.preventDefault();
    mutation.mutate({ email, password });
  };

  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <div className="hidden flex-col justify-between bg-brand p-10 text-brand-foreground lg:flex">
        <div className="flex items-center gap-2">
          <Scale className="size-6 text-accent" />
          <span className="font-serif text-xl font-semibold">LexMind AI</span>
        </div>
        <div>
          <h1 className="font-serif text-3xl font-semibold leading-tight">
            Legal intelligence, structured.
          </h1>
          <ul className="mt-6 space-y-2 text-sm opacity-90">
            <li>• Case Analysis Dashboards</li>
            <li>• IRAC & Case Briefs</li>
            <li>• Grounded research chat over your documents</li>
          </ul>
        </div>
        <p className="text-xs opacity-70">Analysis, not legal advice.</p>
      </div>

      <div className="flex items-center justify-center p-6">
        <form onSubmit={onSubmit} className="w-full max-w-sm space-y-5">
          <div>
            <h2 className="font-serif text-2xl font-semibold">Welcome back</h2>
            <p className="text-sm text-muted-foreground">Log in to your LexMind workspace.</p>
          </div>
          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input id="email" type="email" autoComplete="email" required value={email}
              onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <Input id="password" type="password" autoComplete="current-password" required
              value={password} onChange={(e) => setPassword(e.target.value)} />
          </div>
          <Button type="submit" className="w-full" disabled={mutation.isPending}>
            {mutation.isPending && <Spinner />} Log in
          </Button>
          <p className="text-center text-sm text-muted-foreground">
            New here?{" "}
            <Link to="/register" className="font-medium text-brand hover:underline">
              Create an account
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
}
