export default function HealthDashboard({ dashboard }) {
  if (!dashboard) return null;

  const {
    totalNodes, healthyCount, failedCount, recoveringCount,
    healthyNodes, failedNodes, recoveringNodes,
    cascadeRiskZones, estimatedRecoveryCost, systemHealthPercentage,
  } = dashboard;

  const healthColor =
    systemHealthPercentage >= 80 ? "text-success" :
    systemHealthPercentage >= 50 ? "text-warning" :
    "text-danger";

  return (
    <div className="flex flex-col gap-4">
      {/* Health bar */}
      <div className="rounded-xl border border-border bg-bg-card p-5">
        <div className="flex items-center justify-between mb-3">
          <span className="text-xs font-semibold text-text-primary">System Health</span>
          <span className={`text-3xl font-black ${healthColor}`}>{systemHealthPercentage?.toFixed(0)}%</span>
        </div>
        <div className="h-3 rounded-full bg-bg-primary overflow-hidden">
          <div
            className={`h-full rounded-full transition-all duration-700 ${
              systemHealthPercentage >= 80 ? "bg-success" :
              systemHealthPercentage >= 50 ? "bg-warning" :
              "bg-danger"
            }`}
            style={{ width: `${systemHealthPercentage}%` }}
          />
        </div>
        <div className="flex justify-between text-[10px] text-text-muted mt-2">
          <span>Est. Recovery Cost: {estimatedRecoveryCost?.toFixed(1)}</span>
          <span>{totalNodes} total nodes</span>
        </div>
      </div>

      {/* State counts */}
      <div className="grid grid-cols-3 gap-4">
        <NodeStateGroup label="Healthy" count={healthyCount} nodes={healthyNodes} color="success" />
        <NodeStateGroup label="Failed" count={failedCount} nodes={failedNodes} color="danger" />
        <NodeStateGroup label="Recovering" count={recoveringCount} nodes={recoveringNodes} color="warning" />
      </div>

      {/* Cascade Risk Zones */}
      {cascadeRiskZones && Object.keys(cascadeRiskZones).length > 0 && (
        <div className="rounded-xl border border-danger/20 bg-danger/5 p-4">
          <p className="text-xs font-semibold text-danger mb-3">Cascade Risk Zones</p>
          {Object.entries(cascadeRiskZones).map(([failedNode, atRisk]) => (
            <div key={failedNode} className="mb-2">
              <span className="text-xs font-mono text-danger">{failedNode}</span>
              <span className="text-[10px] text-text-muted"> threatens: </span>
              <div className="flex flex-wrap gap-1 mt-1">
                {atRisk.map((n) => (
                  <span key={n} className="rounded-md bg-danger/10 border border-danger/20 px-2 py-0.5 text-[10px] font-mono text-text-secondary">
                    {n}
                  </span>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function NodeStateGroup({ label, count, nodes, color }) {
  const bgMap = { success: "bg-success/10 border-success/20", danger: "bg-danger/10 border-danger/20", warning: "bg-warning/10 border-warning/20" };
  const textMap = { success: "text-success", danger: "text-danger", warning: "text-warning" };
  const dotMap = { success: "bg-success", danger: "bg-danger", warning: "bg-warning" };

  return (
    <div className={`rounded-xl border ${bgMap[color]} p-4`}>
      <div className="flex items-center gap-2 mb-2">
        <span className={`h-2.5 w-2.5 rounded-full ${dotMap[color]}`} />
        <span className={`text-xs font-semibold ${textMap[color]}`}>{label}</span>
        <span className="ml-auto text-lg font-bold text-text-primary">{count}</span>
      </div>
      <div className="flex flex-wrap gap-1">
        {nodes?.map((n) => (
          <span key={n} className="rounded-md bg-bg-card px-2 py-0.5 text-[10px] font-mono text-text-secondary border border-border">
            {n}
          </span>
        ))}
      </div>
    </div>
  );
}
