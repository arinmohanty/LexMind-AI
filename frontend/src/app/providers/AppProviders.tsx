import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useState, type ReactNode } from "react";
import { Toaster } from "sonner";
import { ThemeProvider } from "@/app/providers/ThemeProvider";
import { AuthProvider } from "@/features/auth/AuthProvider";

export function AppProviders({ children }: { children: ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: { retry: 1, refetchOnWindowFocus: false, staleTime: 30_000 },
        },
      }),
  );

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <AuthProvider>
          {children}
          <Toaster position="top-right" richColors closeButton />
        </AuthProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
