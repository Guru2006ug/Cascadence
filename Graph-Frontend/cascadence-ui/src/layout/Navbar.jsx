import { Link, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";
import { api } from "../api/apiClient";
import { Activity, Zap } from "lucide-react";

const navLinks = [
  { to: "/", label: "Dashboard" },
  { to: "/graph", label: "Graph" },
  { to: "/simulation", label: "Simulation" },
  { to: "/risk", label: "Risk" },
  { to: "/architecture", label: "Architecture" },
  { to: "/health", label: "Health" },
];

export default function Navbar() {
  const { pathname } = useLocation();
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    const check = () => api.get("/graph").then(() => setConnected(true)).catch(() => setConnected(false));
    check();
    const id = setInterval(check, 15000);
    return () => clearInterval(id);
  }, []);

  return (
    <nav className="sticky top-0 z-50 flex items-center justify-between border-b border-border bg-bg-primary/80 backdrop-blur-xl px-6 h-14">
      {/* Logo */}
      <Link to="/" className="flex items-center gap-3 no-underline">
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-accent">
          <Zap className="h-4 w-4 text-white" strokeWidth={2.5} />
        </div>
        <span className="text-[15px] font-semibold tracking-tight text-text-primary">Cascadence</span>
      </Link>

      {/* Nav links */}
      <div className="flex items-center gap-1">
        {navLinks.map(({ to, label }) => {
          const isActive = pathname === to;
          return (
            <Link
              key={to}
              to={to}
              className={`rounded-lg px-3.5 py-2 text-[13px] font-medium no-underline transition-colors ${
                isActive
                  ? "text-text-primary bg-bg-hover"
                  : "text-text-muted hover:text-text-secondary"
              }`}
            >
              {label}
            </Link>
          );
        })}
      </div>

      {/* Right */}
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2" data-tooltip={connected ? "Engine connected" : "Engine offline"}>
          <span className={`flex h-2 w-2 rounded-full ${connected ? "bg-success" : "bg-danger"}`}>
            {connected && <span className="h-2 w-2 rounded-full bg-success animate-ping absolute" />}
          </span>
          <span className={`text-xs font-medium ${connected ? "text-success" : "text-danger"}`}>
            {connected ? "Online" : "Offline"}
          </span>
        </div>
      </div>
    </nav>
  );
}
