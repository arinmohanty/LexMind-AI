import { Outlet } from "react-router-dom";
import { AppBar } from "@/app/layout/AppBar";
import { Sidebar } from "@/app/layout/Sidebar";

export function AppShell() {
  return (
    <div className="flex h-screen overflow-hidden">
      <Sidebar />
      <div className="flex flex-1 flex-col overflow-hidden">
        <AppBar />
        <main className="flex-1 overflow-y-auto p-6">
          <div className="mx-auto max-w-[1280px]">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
