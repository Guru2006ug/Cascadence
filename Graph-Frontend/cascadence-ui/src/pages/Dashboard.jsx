import { useEffect } from "react";
import { useGraphStore } from "../store/graphStore";
import { Link } from "react-router-dom";
import { Share2, Zap, BarChart3, Triangle, Heart, GitFork, Cpu, ArrowRight } from "lucide-react";

const quickActions = [
  { label: "Build Graph", to: "/graph", icon: Share2, desc: "Add services & dependencies" },
  { label: "Run Cascade", to: "/simulation", icon: Zap, desc: "Simulate failure propagation" },
  { label: "Risk Analysis", to: "/risk", icon: BarChart3, desc: "Monte Carlo & sensitivity" },
  { label: "Architecture", to: "/architecture", icon: Triangle, desc: "Score & grade your system" },
  { label: "Health Monitor", to: "/health", icon: Heart, desc: "Live system health" },
  { label: "What-If", to: "/scenario", icon: GitFork, desc: "Experiment with changes" },
  { label: "Optimization", to: "/optimization", icon: Cpu, desc: "Dijkstra, MST & clusters" },
];

export default function Dashboard() {
  const { serviceCount, dependencyCount, fetchGraph } = useGraphStore();

  useEffect(() => {
    fetchGraph();
  }, [fetchGraph]);

  return (
    <div className="flex flex-col gap-12 p-10 max-w-5xl">
      {/* Hero */}
      <div className="animate-in flex flex-col gap-5">
        <h1 className="text-4xl font-bold tracking-tight leading-tight">
          Break Your System<br />
          <span className="text-text-muted">Constructively</span>
        </h1>
        <p className="max-w-md text-base text-text-secondary leading-relaxed">
          Distributed resilience simulation & architecture analysis platform.
          Model, stress-test, and harden your microservices.
        </p>
        <Link to="/graph" className="btn-primary self-start mt-1 no-underline">
          Get started <ArrowRight className="h-4 w-4" />
        </Link>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-4">
        <Link to="/graph" className="stat-card no-underline group">
          <p className="text-3xl font-bold tracking-tight text-text-primary">{serviceCount}</p>
          <p className="text-sm text-text-muted mt-1">Services</p>
        </Link>
        <Link to="/graph" className="stat-card no-underline group">
          <p className="text-3xl font-bold tracking-tight text-text-primary">{dependencyCount}</p>
          <p className="text-sm text-text-muted mt-1">Dependencies</p>
        </Link>
        <div className="stat-card">
          <p className="text-3xl font-bold tracking-tight text-text-primary">
            {serviceCount > 0 && dependencyCount > 0 ? (dependencyCount / serviceCount).toFixed(1) : "—"}
          </p>
          <p className="text-sm text-text-muted mt-1">Avg connections</p>
        </div>
      </div>

      {/* Quick Actions */}
      <div>
        <p className="section-label mb-4">Quick actions</p>
        <div className="grid grid-cols-3 gap-3 stagger">
          {quickActions.map(({ label, to, icon: Icon, desc }) => (
            <Link
              key={to}
              to={to}
              className="group flex items-start gap-4 rounded-2xl border border-border bg-bg-card p-5 no-underline transition-all hover:border-border-light hover:bg-bg-elevated"
            >
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-bg-hover text-text-muted group-hover:text-text-primary transition-colors">
                <Icon className="h-5 w-5" strokeWidth={1.5} />
              </div>
              <div className="flex flex-col gap-1 min-w-0">
                <p className="text-sm font-medium text-text-primary">{label}</p>
                <p className="text-xs text-text-muted leading-relaxed">{desc}</p>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
