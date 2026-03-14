import { NavLink, useLocation } from "react-router-dom";
import { LayoutDashboard, Share2, Zap, BarChart3, GitFork, Triangle, Heart, Lightbulb, Cpu } from "lucide-react";

const sections = [
  {
    title: "Core",
    links: [
      { to: "/", icon: LayoutDashboard, label: "Dashboard" },
      { to: "/graph", icon: Share2, label: "Graph Model" },
    ],
  },
  {
    title: "Simulation",
    links: [
      { to: "/simulation", icon: Zap, label: "Cascade" },
      { to: "/risk", icon: BarChart3, label: "Risk Analysis" },
      { to: "/scenario", icon: GitFork, label: "What-If" },
    ],
  },
  {
    title: "Intelligence",
    links: [
      { to: "/architecture", icon: Triangle, label: "Architecture" },
      { to: "/health", icon: Heart, label: "Health" },
      { to: "/recommendations", icon: Lightbulb, label: "Suggestions" },
      { to: "/optimization", icon: Cpu, label: "Optimization" },
    ],
  },
];

export default function Sidebar() {
  const { pathname } = useLocation();

  return (
    <aside className="flex w-56 shrink-0 flex-col border-r border-border bg-bg-primary">
      <div className="flex flex-col gap-6 py-5 px-3 flex-1 overflow-y-auto">
        {sections.map((section) => (
          <div key={section.title}>
            <p className="mb-2 px-3 section-label">
              {section.title}
            </p>
            <div className="flex flex-col gap-0.5">
              {section.links.map(({ to, icon: Icon, label }) => {
                const isActive = pathname === to;
                return (
                  <NavLink
                    key={to}
                    to={to}
                    className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-[13px] no-underline transition-colors ${
                      isActive
                        ? "bg-bg-hover text-text-primary font-medium"
                        : "text-text-muted hover:bg-bg-hover/50 hover:text-text-secondary"
                    }`}
                  >
                    <Icon className="h-4 w-4 shrink-0" strokeWidth={isActive ? 2 : 1.5} />
                    {label}
                  </NavLink>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </aside>
  );
}
