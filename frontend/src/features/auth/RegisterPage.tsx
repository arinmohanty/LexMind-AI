import { useMutation } from "@tanstack/react-query";
import { Scale } from "lucide-react";
import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Spinner } from "@/components/domain";
import { authApi } from "@/features/auth/api";
import { useAuth } from "@/features/auth/AuthProvider";
import { getErrorMessage } from "@/lib/utils";

const ROLES = [
  { value: "LAW_STUDENT", label: "Law Student" },
  { value: "ADVOCATE", label: "Advocate" },
  { value: "RESEARCHER", label: "Researcher" },
  { value: "LAW_FIRM_ADMIN", label: "Law Firm Admin" },
];

export function RegisterPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("LAW_STUDENT");
  const [organizationName, setOrganizationName] = useState("");
  const [accepted, setAccepted] = useState(false);

  const mutation = useMutation({
    mutationFn: authApi.register,
    onSuccess: (auth) => {
      login(auth);
      navigate("/app/dashboard", { replace: true });
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  });

  const onSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!accepted) {
      toast.error("Please acknowledge the terms and 'no legal advice' notice");
      return;
    }
    mutation.mutate({
      fullName,
      email,
      password,
      role,
      organizationName: role === "LAW_FIRM_ADMIN" ? organizationName : undefined,
    });
  };

  return (
    <div className="flex min-h-screen items-center justify-center p-6">
      <form onSubmit={onSubmit} className="w-full max-w-md space-y-4">
        <div className="flex items-center gap-2">
          <Scale className="size-6 text-accent" />
          <span className="font-serif text-xl font-semibold">LexMind AI</span>
        </div>
        <div>
          <h2 className="font-serif text-2xl font-semibold">Create your account</h2>
          <p className="text-sm text-muted-foreground">Start analyzing cases in minutes.</p>
        </div>

        <div className="space-y-2">
          <Label htmlFor="fullName">Full name</Label>
          <Input id="fullName" required value={fullName} onChange={(e) => setFullName(e.target.value)} />
        </div>
        <div className="space-y-2">
          <Label htmlFor="email">Email</Label>
          <Input id="email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
        </div>
        <div className="space-y-2">
          <Label htmlFor="password">Password</Label>
          <Input id="password" type="password" required minLength={8} value={password}
            onChange={(e) => setPassword(e.target.value)} />
          <p className="text-xs text-muted-foreground">At least 8 characters.</p>
        </div>
        <div className="space-y-2">
          <Label htmlFor="role">I am a…</Label>
          <select
            id="role"
            value={role}
            onChange={(e) => setRole(e.target.value)}
            className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
          >
            {ROLES.map((r) => (
              <option key={r.value} value={r.value}>
                {r.label}
              </option>
            ))}
          </select>
        </div>
        {role === "LAW_FIRM_ADMIN" && (
          <div className="space-y-2">
            <Label htmlFor="org">Firm name</Label>
            <Input id="org" value={organizationName} onChange={(e) => setOrganizationName(e.target.value)} />
          </div>
        )}
        <label className="flex items-start gap-2 text-sm text-muted-foreground">
          <input type="checkbox" checked={accepted} onChange={(e) => setAccepted(e.target.checked)}
            className="mt-1" />
          I accept the Terms and understand LexMind provides legal analysis, not legal advice.
        </label>

        <Button type="submit" className="w-full" disabled={mutation.isPending}>
          {mutation.isPending && <Spinner />} Create account
        </Button>
        <p className="text-center text-sm text-muted-foreground">
          Already have an account?{" "}
          <Link to="/login" className="font-medium text-brand hover:underline">
            Log in
          </Link>
        </p>
      </form>
    </div>
  );
}
