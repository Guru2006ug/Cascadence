import { useState, useEffect } from "react";
import { useGraphStore } from "../store/graphStore";
import { useSimulationStore } from "../store/simulationStore";
import CascadePanel from "../components/simulation/CascadePanel";
import TemporalTimeline from "../components/simulation/TemporalTimeline";

export default function Simulation() {
  const { nodes, fetchGraph } = useGraphStore();
  const {
    runCascade, runTemporalCascade,
    cascadeResult, temporalResult,
    loading, error, clearError, clearResults,
  } = useSimulationStore();

  const [selectedNode, setSelectedNode] = useState("");
  const [mode, setMode] = useState("cascade");

  useEffect(() => { fetchGraph(); }, [fetchGraph]);

  const handleRun = () => {
    if (!selectedNode) return;
    if (mode === "cascade") runCascade(selectedNode);
    else runTemporalCascade(selectedNode);
  };

  return (
    <div className="flex flex-col gap-8 p-10 overflow-y-auto max-w-5xl">
      {/* Header */}
      <div className="animate-in">
        <h1 className="text-2xl font-bold tracking-tight">Cascade Simulation</h1>
        <p className="mt-2 text-sm text-text-secondary">Simulate deterministic & temporal cascading failures</p>
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
          <label className="section-label">Mode</label>
          <div className="flex rounded-lg border border-border overflow-hidden">
            {[{key:"cascade",label:"Deterministic"},{key:"temporal",label:"Temporal"}].map(({key,label})=>(
              <button key={key} onClick={()=>setMode(key)} className={`px-4 py-2.5 text-sm font-medium transition-colors ${mode===key?"bg-accent text-white":"bg-bg-primary text-text-muted hover:bg-bg-hover hover:text-text-secondary"}`}>{label}</button>
            ))}
          </div>
        </div>

        <button onClick={handleRun} disabled={!selectedNode||loading} className="btn-danger">
          {loading ? "Simulating..." : "Trigger Failure"}
        </button>

        {(cascadeResult||temporalResult)&&(
          <button onClick={clearResults} className="btn-ghost">Clear Results</button>
        )}
      </div>

      {!cascadeResult && !temporalResult && !loading && (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-bg-card p-16 text-center">
          <p className="text-text-muted text-sm">Select a service and trigger a failure to see the cascade</p>
        </div>
      )}

      {/* Results */}
      {mode === "cascade" && cascadeResult && <CascadePanel cascadeResult={cascadeResult} />}
      {mode === "temporal" && temporalResult && <TemporalTimeline temporalResult={temporalResult} />}
    </div>
  );
}
