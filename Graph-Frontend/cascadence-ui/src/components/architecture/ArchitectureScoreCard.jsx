import { RadarChart, Radar, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer, Tooltip } from "recharts";

export default function ArchitectureScoreCard({ scoreResult }) {
  if (!scoreResult) return null;

  const gradeColors = {
    A: "text-success border-success/30 bg-success/10",
    B: "text-info border-info/30 bg-info/10",
    C: "text-warning border-warning/30 bg-warning/10",
    D: "text-danger border-danger/30 bg-danger/10",
    F: "text-danger border-danger/50 bg-danger/15",
  };

  const radarData = [
    { metric: "Resilience", value: +(scoreResult.resilienceScore * 100).toFixed(1) },
    { metric: "Redundancy", value: +(scoreResult.redundancyScore * 100).toFixed(1) },
    { metric: "Coupling", value: +(scoreResult.couplingScore * 100).toFixed(1) },
    { metric: "Depth", value: +(scoreResult.depthScore * 100).toFixed(1) },
    { metric: "SPOF", value: +(scoreResult.spofScore * 100).toFixed(1) },
  ];

  return (
    <div className="flex flex-col gap-4">
      {/* Score + Grade */}
      <div className="flex items-center gap-6">
        <div className="flex flex-col items-center gap-1">
          <span className={`text-6xl font-black ${
            scoreResult.compositeScore >= 80 ? "text-success" :
            scoreResult.compositeScore >= 65 ? "text-info" :
            scoreResult.compositeScore >= 50 ? "text-warning" :
            "text-danger"
          }`}>
            {scoreResult.compositeScore?.toFixed(0)}
          </span>
          <span className="text-xs text-text-muted">out of 100</span>
        </div>
        <span className={`flex h-16 w-16 items-center justify-center rounded-2xl border-2 text-3xl font-black ${gradeColors[scoreResult.grade] || gradeColors.F}`}>
          {scoreResult.grade}
        </span>
        <div className="flex flex-col gap-1 text-xs text-text-muted">
          <span>{scoreResult.totalNodes} services • {scoreResult.totalEdges} dependencies</span>
          <span>{scoreResult.spofCount} single points of failure</span>
        </div>
      </div>

      {/* Radar Chart */}
      <div className="card">
        <p className="text-xs font-semibold text-text-primary mb-2">Sub-Metric Breakdown</p>
        <ResponsiveContainer width="100%" height={300}>
          <RadarChart data={radarData}>
            <PolarGrid stroke="#27272a" />
            <PolarAngleAxis dataKey="metric" tick={{ fill: "#a1a1aa", fontSize: 11 }} />
            <PolarRadiusAxis domain={[0, 100]} tick={{ fill: "#52525b", fontSize: 9 }} />
            <Tooltip contentStyle={{ background: "#18181b", border: "1px solid #27272a", borderRadius: "10px", fontSize: "12px", color: "#fafafa", boxShadow: "0 8px 32px rgba(0,0,0,0.4)" }} formatter={(val) => [`${val}%`, ""]} />
            <Radar dataKey="value" stroke="#7c3aed" fill="#7c3aed" fillOpacity={0.25} strokeWidth={2} />
          </RadarChart>
        </ResponsiveContainer>
      </div>

      {/* Metric Bars */}
      <div className="grid grid-cols-5 gap-3">
        {radarData.map(({ metric, value }) => {
          const barColor =
            value >= 80 ? "bg-success" :
            value >= 65 ? "bg-info" :
            value >= 50 ? "bg-warning" :
            "bg-danger";
          return (
            <div key={metric} className="rounded-xl border border-border bg-bg-card p-3 text-center">
              <p className="text-lg font-bold text-text-primary">{value}%</p>
              <div className="mt-1.5 h-1.5 rounded-full bg-bg-primary overflow-hidden">
                <div className={`h-full rounded-full ${barColor} transition-all`} style={{ width: `${value}%` }} />
              </div>
              <p className="text-[10px] text-text-muted mt-1.5">{metric}</p>
            </div>
          );
        })}
      </div>

      {/* Recommendations */}
      {scoreResult.recommendations?.length > 0 && (
        <div className="card">
          <p className="text-xs font-semibold text-text-primary mb-2">Suggestions</p>
          <ul className="space-y-1.5">
            {scoreResult.recommendations.map((rec, i) => (
              <li key={i} className="flex items-start gap-2 text-xs text-text-secondary">
                <span className="text-accent">•</span>
                {rec}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
