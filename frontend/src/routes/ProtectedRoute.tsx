import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "@/features/auth/AuthProvider";

/** Gates the authenticated app. Unauthenticated users go to /login with a return path. */
export function ProtectedRoute() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }
  return <Outlet />;
}
