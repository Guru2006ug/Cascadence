import { useState } from "react";
import { optimizationService } from "../../services/optimizationService";

export default function MSTPanel() {
  const [mst, setMst] = useState(null);
  const [elapsed, setElapsed] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const run = async () => {
    setLoading(true);
    setError(null);
    const t0 = performance.now();
    try {
      const res = await optimizationService.getMST();
      setElapsed(performance.now() - t0);
      setMst(res.data);
    } catch (err) {
      setError(err.response?.data?.error || "MST computation failed");
    } finally {
      setLoading(false);
    }
  };

  const edges = mst?.edges || mst || [];
  const totalCost = edges.reduce((sum, e) => sum + (e.infraCost || 0), 0);

  return (
    <div className="card flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold text-text-primary">Minimum Spanning Tree</p>
          <p className="text-[10px] text-text-muted">Kruskal's — minimum infrastructure cost backbone</p>
        </div>
        <div className="flex items-center gap-2">
          {elapsed != null && (
            <span className="rounded bg-success/10 px-2 py-0.5 text-[10px] font-mono text-success">{elapsed.toFixed(1)}ms</span>
          )}
          <button onClick={run} disabled={loading} className="btn-primary !py-1.5 !px-4 !text-xs">
            {loading ? "..." : "Compute"}
          </button>
        </div>
      </div>

      {error && <p className="text-xs text-danger">{error}</p>}

      {edges.length > 0 && (
        <>
          <div className="grid grid-cols-3 gap-3">
            <div className="rounded-lg border border-border bg-bg-primary p-3 text-center">
              <p className="text-lg font-bold text-text-primary">{edges.length}</p>
              <p className="text-[10px] text-text-muted">Edges</p>
            </div>
            <div className="rounded-lg border border-border bg-bg-primary p-3 text-center">
              <p className="text-lg font-bold text-success">{totalCost.toFixed(1)}</p>
              <p className="text-[10px] text-text-muted">Total Cost</p>
            </div>
            <div className="rounded-lg border border-border bg-bg-primary p-3 text-center">
              <p className="text-lg font-bold text-info">{(totalCost / (edges.length || 1)).toFixed(2)}</p>
              <p className="text-[10px] text-text-muted">Avg Cost</p>
            </div>
          </div>

          <div className="rounded-lg border border-border overflow-hidden">
            <table className="w-full text-xs">
              <thead>
                <tr className="border-b border-border bg-bg-secondary">
                  <th className="py-1.5 px-3 text-left text-[10px] text-text-muted font-semibold">From</th>
                  <th className="py-1.5 px-3 text-left text-[10px] text-text-muted font-semibold">To</th>
                  <th className="py-1.5 px-3 text-right text-[10px] text-text-muted font-semibold">Probability</th>
                  <th className="py-1.5 px-3 text-right text-[10px] text-text-muted font-semibold">Cost</th>
                </tr>
              </thead>
              <tbody>
                {edges.map((e, i) => (
                  <tr key={i} className="border-b border-border last:border-0 hover:bg-bg-hover">
                    <td className="py-1.5 px-3 font-mono text-text-secondary">{e.from}</td>
                    <td className="py-1.5 px-3 font-mono text-text-secondary">{e.to}</td>
                    <td className="py-1.5 px-3 text-right">
                      <span className={`font-mono ${e.failureProbability >= 0.7 ? "text-danger" : e.failureProbability >= 0.4 ? "text-warning" : "text-success"}`}>
                        {(e.failureProbability != null ? e.failureProbability : 0).toFixed(2)}
                      </span>
                    </td>
                    <td className="py-1.5 px-3 text-right font-mono text-text-primary">{(e.infraCost || 0).toFixed(1)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
