import { useState } from "react";
import { useGraphStore } from "../../store/graphStore";
import { optimizationService } from "../../services/optimizationService";

export default function DijkstraPanel() {
  const { nodes } = useGraphStore();
  const [source, setSource] = useState("");
  const [result, setResult] = useState(null);
  const [elapsed, setElapsed] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const run = async () => {
    if (!source) return;
    setLoading(true);
    setError(null);
    const t0 = performance.now();
    try {
      const res = await optimizationService.getDijkstra(source);
      setElapsed(performance.now() - t0);
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.error || "Dijkstra failed");
    } finally {
      setLoading(false);
    }
  };

  const entries = result ? Object.entries(result).sort((a, b) => a[1] - b[1]) : [];

  return (
    <div className="card flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold text-text-primary">Shortest Paths — Dijkstra</p>
          <p className="text-[10px] text-text-muted">Minimum propagation cost from source to all nodes</p>
        </div>
        {elapsed != null && (
          <span className="rounded bg-accent/10 px-2 py-0.5 text-[10px] font-mono text-accent">{elapsed.toFixed(1)}ms</span>
        )}
      </div>

      <div className="flex gap-2">
        <select value={source} onChange={(e) => setSource(e.target.value)} className="input flex-1">
          <option value="">Source node...</option>
          {nodes.map((n) => <option key={n.id} value={n.id}>{n.id}</option>)}
        </select>
        <button onClick={run} disabled={!source || loading} className="btn-primary !py-1.5 !px-4 !text-xs">
          {loading ? "..." : "Run"}
        </button>
      </div>

      {error && <p className="text-xs text-danger">{error}</p>}

      {entries.length > 0 && (
        <div className="divide-y divide-border">
          {entries.map(([node, dist]) => (
            <div key={node} className="flex items-center justify-between py-1.5">
              <span className="text-xs text-text-secondary">{node}</span>
              <div className="flex items-center gap-2">
                <div className="h-1 rounded-full bg-accent/20" style={{ width: "80px" }}>
                  <div
                    className="h-1 rounded-full bg-accent"
                    style={{ width: `${Math.min((dist / Math.max(...entries.map(e => e[1]), 1)) * 100, 100)}%` }}
                  />
                </div>
                <span className={`text-xs font-mono ${dist === Infinity || dist >= 999999 ? "text-danger" : "text-text-primary"}`}>
                  {dist === Infinity || dist >= 999999 ? "∞" : dist.toFixed(2)}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
