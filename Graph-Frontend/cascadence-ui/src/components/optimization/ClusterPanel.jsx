import { useState } from "react";
import { optimizationService } from "../../services/optimizationService";

const COLORS = ["#7c3aed", "#06b6d4", "#f59e0b", "#10b981", "#ef4444", "#8b5cf6", "#fb923c", "#14b8a6"];

export default function ClusterPanel() {
  const [clusters, setClusters] = useState(null);
  const [elapsed, setElapsed] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const run = async () => {
    setLoading(true);
    setError(null);
    const t0 = performance.now();
    try {
      const res = await optimizationService.getClusters();
      setElapsed(performance.now() - t0);
      setClusters(res.data);
    } catch (err) {
      setError(err.response?.data?.error || "Cluster detection failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold text-text-primary">Correlation Clusters</p>
          <p className="text-[10px] text-text-muted">DSU-based failure group detection</p>
        </div>
        <div className="flex items-center gap-2">
          {elapsed != null && (
            <span className="rounded bg-info/10 px-2 py-0.5 text-[10px] font-mono text-info">{elapsed.toFixed(1)}ms</span>
          )}
          <button onClick={run} disabled={loading} className="btn-primary !py-1.5 !px-4 !text-xs">
            {loading ? "..." : "Detect"}
          </button>
        </div>
      </div>

      {error && <p className="text-xs text-danger">{error}</p>}

      {clusters && clusters.length > 0 && (
        <div className="grid gap-2">
          {clusters.map((cluster, idx) => (
            <div key={idx} className="rounded-lg border border-border bg-bg-primary p-3 flex items-center gap-3">
              <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-[10px] font-bold text-white" style={{ background: COLORS[idx % COLORS.length] }}>
                C{idx + 1}
              </div>
              <div className="flex-1">
                <p className="text-xs text-text-secondary">
                  {cluster.representative && <span className="text-text-primary font-medium">{cluster.representative} </span>}
                  <span className="text-text-muted">· {cluster.members?.length || 0} nodes</span>
                </p>
                <div className="mt-1 flex flex-wrap gap-1">
                  {(cluster.members || []).map((m) => (
                    <span key={m} className="rounded bg-bg-secondary px-1.5 py-0.5 text-[10px] text-text-secondary font-mono">{m}</span>
                  ))}
                </div>
              </div>
            </div>
          ))}
          <p className="text-[10px] text-text-muted">{clusters.length} cluster(s) detected</p>
        </div>
      )}

      {clusters && clusters.length === 0 && (
        <p className="text-xs text-text-muted text-center py-4">No clusters detected — each node is independent</p>
      )}
    </div>
  );
}
