import { useSimulationStore } from "../../store/simulationStore";

export default function CascadePanel({ cascadeResult }) {
  if (!cascadeResult) return null;

  const {
    failedNode,
    affectedNodes,
    depthMap,
    cascadeDepth,
    impactScore,
    weightedImpactScore,
    affectedCount,
    totalNodes,
  } = cascadeResult;

  const impactPercent = (impactScore * 100).toFixed(1);
  const severity =
    impactScore >= 0.7 ? { label: "CRITICAL", color: "text-danger bg-danger/10" } :
    impactScore >= 0.4 ? { label: "HIGH", color: "text-warning bg-warning/10" } :
    impactScore >= 0.2 ? { label: "MEDIUM", color: "text-info bg-info/10" } :
    { label: "LOW", color: "text-success bg-success/10" };

  // Group affected nodes by cascade depth level
  const levels = {};
  if (depthMap) {
    Object.entries(depthMap).forEach(([node, depth]) => {
      if (!levels[depth]) levels[depth] = [];
      levels[depth].push(node);
    });
  }

  return (
    <div className="flex flex-col gap-4">
      {/* Header Stats */}
      <div className="grid grid-cols-4 gap-3">
        <div className="rounded-xl border border-border bg-bg-card p-3 text-center">
          <p className="text-lg font-bold text-danger">{affectedCount}</p>
          <p className="text-[10px] text-text-muted">Affected</p>
        </div>
        <div className="rounded-xl border border-border bg-bg-card p-3 text-center">
          <p className="text-lg font-bold text-text-primary">{totalNodes}</p>
          <p className="text-[10px] text-text-muted">Total</p>
        </div>
        <div className="rounded-xl border border-border bg-bg-card p-3 text-center">
          <p className="text-lg font-bold text-accent">{cascadeDepth}</p>
          <p className="text-[10px] text-text-muted">Depth</p>
        </div>
        <div className="rounded-xl border border-border bg-bg-card p-3 text-center">
          <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${severity.color}`}>
            {severity.label}
          </span>
          <p className="text-[10px] text-text-muted mt-1">{impactPercent}%</p>
        </div>
      </div>

      {/* Weighted Impact */}
      <div className="card">
        <div className="flex justify-between text-xs text-text-muted mb-2">
          <span>Impact Score</span>
          <span className="font-mono text-text-secondary">{impactPercent}%</span>
        </div>
        <div className="h-2 rounded-full bg-bg-primary overflow-hidden">
          <div
            className="h-full rounded-full bg-gradient-to-r from-success via-warning to-danger transition-all duration-500"
            style={{ width: `${impactPercent}%` }}
          />
        </div>
        <div className="flex justify-between text-[10px] text-text-muted mt-2">
          <span>Weighted: {weightedImpactScore?.toFixed(3)}</span>
          <span>Origin: {failedNode}</span>
        </div>
      </div>

      {/* Cascade Levels */}
      <div className="card">
        <p className="text-xs font-semibold text-text-primary mb-3">Cascade Chain</p>
        <div className="flex flex-col gap-2">
          {Object.entries(levels)
            .sort(([a], [b]) => Number(a) - Number(b))
            .map(([depth, nodeList]) => (
              <div key={depth} className="flex items-start gap-3">
                <span className="shrink-0 flex h-6 w-6 items-center justify-center rounded-full bg-accent/15 text-[10px] font-bold text-accent">
                  L{depth}
                </span>
                <div className="flex flex-wrap gap-1.5">
                  {nodeList.map((node) => (
                    <span
                      key={node}
                      className={`rounded-md px-2 py-0.5 text-[11px] font-mono ${
                        node === failedNode
                          ? "bg-danger/15 text-danger border border-danger/30"
                          : "bg-bg-hover text-text-secondary border border-border"
                      }`}
                    >
                      {node}
                    </span>
                  ))}
                </div>
              </div>
            ))}
        </div>
      </div>
    </div>
  );
}
