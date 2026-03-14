import { RadarChart, Radar, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer, Legend, Tooltip } from "recharts";

export default function ArchitectureComparison({ comparisonResult }) {
  if (!comparisonResult) return null;

  const { currentScore, proposedScore, compositeScoreDelta, verdict, improvements, degradations } = comparisonResult;

  const radarData = [
    { metric: "Resilience", current: +(currentScore.resilienceScore * 100).toFixed(1), proposed: +(proposedScore.resilienceScore * 100).toFixed(1) },
    { metric: "Redundancy", current: +(currentScore.redundancyScore * 100).toFixed(1), proposed: +(proposedScore.redundancyScore * 100).toFixed(1) },
    { metric: "Coupling", current: +(currentScore.couplingScore * 100).toFixed(1), proposed: +(proposedScore.couplingScore * 100).toFixed(1) },
    { metric: "Depth", current: +(currentScore.depthScore * 100).toFixed(1), proposed: +(proposedScore.depthScore * 100).toFixed(1) },
    { metric: "SPOF", current: +(currentScore.spofScore * 100).toFixed(1), proposed: +(proposedScore.spofScore * 100).toFixed(1) },
  ];

  const deltaColor = compositeScoreDelta > 0 ? "text-success" : compositeScoreDelta < 0 ? "text-danger" : "text-text-muted";
  const deltaIcon = compositeScoreDelta > 0 ? "↑" : compositeScoreDelta < 0 ? "↓" : "→";

  return (
    <div className="flex flex-col gap-4">
      {/* Score comparison */}
      <div className="flex items-center justify-center gap-8 rounded-xl border border-border bg-bg-card p-6">
        <div className="text-center">
          <p className="text-4xl font-black text-text-primary">{currentScore.compositeScore?.toFixed(0)}</p>
          <p className="text-xs text-text-muted">Current ({currentScore.grade})</p>
        </div>
        <span className={`text-3xl font-bold ${deltaColor}`}>
          {deltaIcon} {Math.abs(compositeScoreDelta).toFixed(1)}
        </span>
        <div className="text-center">
          <p className="text-4xl font-black text-accent">{proposedScore.compositeScore?.toFixed(0)}</p>
          <p className="text-xs text-text-muted">Proposed ({proposedScore.grade})</p>
        </div>
      </div>

      {/* Verdict */}
      <div className={`rounded-xl border px-4 py-3 text-sm ${
        compositeScoreDelta > 0 ? "border-success/30 bg-success/10 text-success" :
        compositeScoreDelta < 0 ? "border-danger/30 bg-danger/10 text-danger" :
        "border-border bg-bg-card text-text-secondary"
      }`}>
        {verdict}
      </div>

      {/* Radar overlay */}
      <div className="card">
        <p className="text-xs font-semibold text-text-primary mb-2">Metric Comparison</p>
        <ResponsiveContainer width="100%" height={300}>
          <RadarChart data={radarData}>
            <PolarGrid stroke="#27272a" />
            <PolarAngleAxis dataKey="metric" tick={{ fill: "#a1a1aa", fontSize: 11 }} />
            <PolarRadiusAxis domain={[0, 100]} tick={{ fill: "#52525b", fontSize: 9 }} />
            <Tooltip contentStyle={{ background: "#18181b", border: "1px solid #27272a", borderRadius: "10px", fontSize: "12px", color: "#fafafa", boxShadow: "0 8px 32px rgba(0,0,0,0.4)" }} />
            <Radar dataKey="current" stroke="#52525b" fill="#52525b" fillOpacity={0.15} strokeWidth={2} name="Current" />
            <Radar dataKey="proposed" stroke="#7c3aed" fill="#7c3aed" fillOpacity={0.2} strokeWidth={2} name="Proposed" />
            <Legend wrapperStyle={{ fontSize: "11px" }} />
          </RadarChart>
        </ResponsiveContainer>
      </div>

      {/* Improvements / Degradations */}
      <div className="grid grid-cols-2 gap-4">
        {improvements?.length > 0 && (
          <div className="rounded-xl border border-success/20 bg-success/5 p-3">
            <p className="text-xs font-semibold text-success mb-2">Improvements</p>
            {improvements.map((item, i) => (
              <p key={i} className="text-xs text-text-secondary">↑ {item}</p>
            ))}
          </div>
        )}
        {degradations?.length > 0 && (
          <div className="rounded-xl border border-danger/20 bg-danger/5 p-3">
            <p className="text-xs font-semibold text-danger mb-2">Degradations</p>
            {degradations.map((item, i) => (
              <p key={i} className="text-xs text-text-secondary">↓ {item}</p>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
