import { useState, useEffect } from "react";
import { useGraphStore } from "../store/graphStore";
import { useSimulationStore } from "../store/simulationStore";
import ProbabilityChart from "../components/analytics/ProbabilityChart";
import SensitivityChart from "../components/analytics/SensitivityChart";
import CriticalNodesTable from "../components/analytics/CriticalNodesTable";

export default function RiskAnalysis() {
  const { nodes, fetchGraph } = useGraphStore();
  const {
    runMonteCarlo, fetchCriticalNodes, fetchResilience, fetchSensitivity,
    monteCarloResult, criticalNodes, resilience, sensitivity,
    loading, error, clearError,
  } = useSimulationStore();

  const [selectedNode, setSelectedNode] = useState("");
  const [iterations, setIterations] = useState(1000);

  useEffect(() => { fetchGraph(); }, [fetchGraph]);

  const handleMonteCarlo = () => {
    if (!selectedNode) return;
    runMonteCarlo(selectedNode, iterations);
  };

  const handleFullAnalysis = () => {
    fetchCriticalNodes();
    fetchResilience(iterations);
    fetchSensitivity(Math.min(iterations, 500));
  };

  return (
    <div className="flex flex-col gap-8 overflow-y-auto p-10 max-w-5xl">
      {/* Header */}
      <div className="animate-in">
        <h1 className="text-2xl font-bold tracking-tight">Risk Analysis</h1>
        <p className="mt-2 text-sm text-text-secondary">Monte Carlo simulation, critical nodes & sensitivity analysis</p>
      </div>

      {error && <div className="error-banner"><span>{error}</span><button onClick={clearError} className="text-danger/60 hover:text-danger">✕</button></div>}

      {/* Controls */}
      <div className="card flex flex-wrap items-end gap-4">
        <div className="flex flex-col gap-1.5">
          <label className="section-label">Trigger Node</label>
          <select value={selectedNode} onChange={(e) => setSelectedNode(e.target.value)} className="input min-w-[200px]">
            <option value="">Select a service...</option>
            {nodes.map((n) => (<option key={n.id} value={n.id}>{n.id}</option>))}
          </select>
        </div>

        <div className="flex flex-col gap-1.5">
          <label className="section-label">Iterations</label>
          <input type="number" min={100} max={50000} step={100} value={iterations} onChange={(e) => setIterations(+e.target.value)} className="input w-32 !font-mono" />
        </div>

        <button onClick={handleMonteCarlo} disabled={!selectedNode||loading} className="btn-primary">
          {loading ? "Running..." : "Monte Carlo"}
        </button>

        <button onClick={handleFullAnalysis} disabled={loading} className="btn-ghost">
          Full System Analysis
        </button>
      </div>

      {/* Resilience Banner */}
      {resilience && (
        <div className="grid grid-cols-3 gap-4">
          <div className="rounded-xl border border-border bg-bg-card p-4 text-center">
            <p className="text-3xl font-bold text-accent">{(resilience.resilienceScore * 100).toFixed(1)}%</p>
            <p className="text-xs text-text-muted mt-1">Resilience Score</p>
          </div>
          <div className="rounded-xl border border-border bg-bg-card p-4 text-center">
            <p className="text-3xl font-bold text-danger">{(resilience.fragilityIndex * 100).toFixed(1)}%</p>
            <p className="text-xs text-text-muted mt-1">Fragility Index</p>
          </div>
          <div className="rounded-xl border border-border bg-bg-card p-4 text-center">
            <p className="text-3xl font-bold text-warning">{resilience.averageCascadeSize?.toFixed(1)}</p>
            <p className="text-xs text-text-muted mt-1">Avg Cascade Size</p>
          </div>
        </div>
      )}

      {/* Monte Carlo Results */}
      {monteCarloResult && (
        <div className="flex flex-col gap-4">
          {/* Summary row */}
          <div className="grid grid-cols-4 gap-3">
            <div className="rounded-xl border border-border bg-bg-card p-3 text-center">
              <p className="text-lg font-bold text-text-primary">{monteCarloResult.iterations}</p>
              <p className="text-[10px] text-text-muted">Iterations</p>
            </div>
            <div className="rounded-xl border border-border bg-bg-card p-3 text-center">
              <p className="text-lg font-bold text-warning">{monteCarloResult.expectedCascadeSize?.toFixed(2)}</p>
              <p className="text-[10px] text-text-muted">Expected Size</p>
            </div>
            <div className="rounded-xl border border-border bg-bg-card p-3 text-center">
              <p className="text-lg font-bold text-danger">{(monteCarloResult.fragilityIndex * 100).toFixed(1)}%</p>
              <p className="text-[10px] text-text-muted">Fragility</p>
            </div>
            <div className="rounded-xl border border-border bg-bg-card p-3 text-center">
              <p className="text-lg font-bold text-accent">{monteCarloResult.weightedExpectedImpact?.toFixed(2)}</p>
              <p className="text-[10px] text-text-muted">Weighted Impact</p>
            </div>
          </div>

          <ProbabilityChart failureProbabilities={monteCarloResult.failureProbabilities} />

          {/* Confidence Intervals */}
          {monteCarloResult.confidenceIntervals && (
            <div className="rounded-xl border border-border bg-bg-card p-4">
              <p className="text-xs font-semibold text-text-primary mb-3">95% Confidence Intervals</p>
              <div className="space-y-2">
                {Object.entries(monteCarloResult.confidenceIntervals)
                  .sort(([, a], [, b]) => b[1] - a[1])
                  .map(([node, [lower, mean, upper]]) => (
                    <div key={node} className="flex items-center gap-3">
                      <span className="w-28 truncate text-xs font-mono text-text-secondary">{node}</span>
                      <div className="flex-1 relative h-5">
                        <div className="absolute inset-y-0 left-0 right-0 bg-bg-primary rounded" />
                        <div
                          className="absolute inset-y-1 bg-accent/20 rounded"
                          style={{
                            left: `${lower * 100}%`,
                            width: `${(upper - lower) * 100}%`,
                          }}
                        />
                        <div
                          className="absolute top-1 h-3 w-0.5 bg-accent rounded"
                          style={{ left: `${mean * 100}%` }}
                        />
                      </div>
                      <span className="text-[10px] font-mono text-text-muted w-32 text-right">
                        [{(lower * 100).toFixed(1)}, {(mean * 100).toFixed(1)}, {(upper * 100).toFixed(1)}]%
                      </span>
                    </div>
                  ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Critical Nodes */}
      {criticalNodes && <CriticalNodesTable criticalData={criticalNodes} />}

      {/* Sensitivity */}
      {sensitivity && <SensitivityChart sensitivityData={sensitivity} />}

      {!monteCarloResult && !criticalNodes && !resilience && !loading && (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-bg-card p-16 text-center">
          <p className="text-text-muted text-sm">Run a Monte Carlo simulation or full system analysis</p>
        </div>
      )}
    </div>
  );
}
