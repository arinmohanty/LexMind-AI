import { Scale, Upload } from "lucide-react";
import { NavLink, useNavigate } from "react-router-dom";
import { navItemsFor } from "@/app/config/navConfig";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/features/auth/AuthProvider";
import { cn } from "@/lib/utils";

export function Sidebar() {
  const { user } = useAuth();
  const navigate = useNavigate();
  if (!user) return null;
  const items = navItemsFor(user.role);

  return (
    <aside className="hidden w-60 shrink-0 flex-col border-r bg-card md:flex">
      <div className="flex h-14 items-center gap-2 border-b px-5">
        <Scale className="size-5 text-accent" />
        <span className="font-serif text-lg font-semibold">LexMind</span>
      </div>

      <nav className="flex-1 space-y-1 p-3">
        {items.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === "/app/dashboard" || to === "/app/admin"}
            className={({ isActive }) =>
              cn(
                "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                isActive
                  ? "bg-brand/10 text-brand"
                  : "text-muted-foreground hover:bg-muted hover:text-foreground",
              )
            }
          >
            <Icon className="size-4" />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="p-3">
        <Button className="w-full" onClick={() => navigate("/app/cases/new")}>
          <Upload className="size-4" /> Upload Case
        </Button>
      </div>
    </aside>
  );
}
