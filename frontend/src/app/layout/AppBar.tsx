import { LogOut, Moon, Sun } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useTheme } from "@/app/providers/ThemeProvider";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/features/auth/AuthProvider";

export function AppBar() {
  const { user, logout } = useAuth();
  const { theme, setTheme } = useTheme();
  const navigate = useNavigate();

  const onLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <header className="flex h-14 items-center justify-between border-b bg-card px-4">
      <div className="text-sm text-muted-foreground">
        {user && (
          <span>
            <span className="font-medium text-foreground">{user.fullName}</span>
            {" · "}
            {user.role.replace("_", " ").toLowerCase()}
          </span>
        )}
      </div>
      <div className="flex items-center gap-2">
        <Button
          variant="ghost"
          size="icon"
          aria-label="Toggle theme"
          onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
        >
          {theme === "dark" ? <Sun className="size-4" /> : <Moon className="size-4" />}
        </Button>
        <Button variant="ghost" size="sm" onClick={onLogout}>
          <LogOut className="size-4" /> Logout
        </Button>
      </div>
    </header>
  );
}
