import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import type { AuthResponse, User } from "@/types/api";
import { clearAuth, getStoredUser, setStoredUser, setTokens } from "@/lib/auth";

interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  login: (auth: AuthResponse) => void;
  logout: () => void;
  setUser: (user: User) => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUserState] = useState<User | null>(() => getStoredUser());

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: !!user,
      login: (auth) => {
        setTokens(auth.accessToken, auth.refreshToken);
        setStoredUser(auth.user);
        setUserState(auth.user);
      },
      logout: () => {
        clearAuth();
        setUserState(null);
      },
      setUser: (u) => {
        setStoredUser(u);
        setUserState(u);
      },
    }),
    [user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
