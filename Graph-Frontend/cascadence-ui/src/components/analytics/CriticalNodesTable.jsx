export default function CriticalNodesTable({ criticalData }) {
  if (!criticalData?.rankings?.length) return null;

  return (
    <div className="card">
      <div className="flex items-center justify-between mb-3">
        <p className="text-xs font-semibold text-text-primary">Critical Node Rankings</p>
        {criticalData.mostCritical && (
          <span className="text-[10px] font-mono bg-danger/10 text-danger px-2 py-0.5 rounded-full">
            Most Critical: {criticalData.mostCritical}
          </span>
        )}
      </div>
      <div className="overflow-x-auto">
        <table className="w-full text-xs">
          <thead>
            <tr className="border-b border-border text-[10px] uppercase text-text-muted">
              <th className="py-2 text-left font-semibold">#</th>
              <th className="py-2 text-left font-semibold">Node</th>
              <th className="py-2 text-right font-semibold">Affected</th>
              <th className="py-2 text-right font-semibold">Impact</th>
              <th className="py-2 text-right font-semibold">Weighted</th>
              <th className="py-2 text-right font-semibold">Depth</th>
              <th className="py-2 text-left font-semibold pl-3">Severity</th>
            </tr>
          </thead>
          <tbody>
            {criticalData.rankings.map((node, idx) => {
              const severity =
                node.impactScore >= 0.7 ? { label: "CRITICAL", cls: "bg-danger/15 text-danger" } :
                node.impactScore >= 0.4 ? { label: "HIGH", cls: "bg-warning/15 text-warning" } :
                node.impactScore >= 0.2 ? { label: "MEDIUM", cls: "bg-info/15 text-info" } :
                { label: "LOW", cls: "bg-success/15 text-success" };

              return (
                <tr key={node.nodeId} className="border-b border-border/50 hover:bg-bg-hover transition-colors">
                  <td className="py-2.5 font-mono text-text-muted">{idx + 1}</td>
                  <td className="py-2.5 font-semibold text-text-primary">{node.nodeId}</td>
                  <td className="py-2.5 text-right font-mono text-text-secondary">{node.affectedCount}</td>
                  <td className="py-2.5 text-right font-mono text-text-secondary">{(node.impactScore * 100).toFixed(1)}%</td>
                  <td className="py-2.5 text-right font-mono text-text-secondary">{node.weightedImpactScore?.toFixed(3)}</td>
                  <td className="py-2.5 text-right font-mono text-text-secondary">{node.cascadeDepth}</td>
                  <td className="py-2.5 pl-3">
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${severity.cls}`}>
                      {severity.label}
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
