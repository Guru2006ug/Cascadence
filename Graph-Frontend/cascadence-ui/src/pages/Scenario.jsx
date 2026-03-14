import { useState, useEffect } from "react";
import { useGraphStore } from "../store/graphStore";
import { useSimulationStore } from "../store/simulationStore";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, Cell } from "recharts";

export default function Scenario() {
  const { nodes, edges, fetchGraph } = useGraphStore();
  const { runWhatIf, whatIfResult, loading, error, clearError } = useSimulationStore();

  const [failedNode, setFailedNode] = useState("");
  const [iterations, setIterations] = useState(1000);
  const [edgeUpdates, setEdgeUpdates] = useState([]);
  const [edgeAdditions, setEdgeAdditions] = useState([]);
  const [edgeRemovals, setEdgeRemovals] = useState([]);

  // Temp forms
  const [updateForm, setUpdateForm] = useState({ from: "", to: "", newProbability: 0.3 });
  const [addForm, setAddForm] = useState({ from: "", to: "", probability: 0.5, infraCost: 1 });
  const [removeForm, setRemoveForm] = useState({ from: "", to: "" });

  useEffect(() => { fetchGraph(); }, [fetchGraph]);

  const handleRun = () => {
    if (!failedNode) return;
    runWhatIf({ failedNode, iterations, edgeUpdates, edgeAdditions, edgeRemovals });
  };

  const clearScenario = () => {
    setEdgeUpdates([]);
    setEdgeAdditions([]);
    setEdgeRemovals([]);
  };

  // Comparison chart data
  const comparisonData = whatIfResult?.beforePerNode && whatIfResult?.afterPerNode
    ? Object.keys(whatIfResult.beforePerNode).map((node) => ({
        node,
        before: +(whatIfResult.beforePerNode[node] * 100).toFixed(1),
        after: +(whatIfResult.afterPerNode[node] * 100).toFixed(1),
      })).sort((a, b) => b.before - a.before)
    : [];

  return (
    <div className="flex flex-col gap-8 overflow-y-auto p-10 max-w-6xl">
      <div className="animate-in">
        <h1 className="text-2xl font-bold tracking-tight">Scenario Playground</h1>
        <p className="mt-2 text-sm text-text-secondary">What-if experiments — modify edges, compare before/after resilience</p>
      </div>

      {error && <div className="error-banner"><span>{error}</span><button onClick={clearError} className="text-danger/60 hover:text-danger">✕</button></div>}

      {/* Scenario Builder */}
      <div className="grid grid-cols-2 gap-4">
        {/* Left: Config */}
        <div className="flex flex-col gap-4 card">
          <p className="text-xs font-semibold text-text-primary">Scenario Config</p>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-[10px] text-text-muted">Trigger Node</label>
              <select
                value={failedNode}
                onChange={(e) => setFailedNode(e.target.value)}
                className="w-full rounded-lg border border-border bg-bg-primary px-3 py-2 text-xs text-text-primary focus:border-accent focus:outline-none"
              >
                <option value="">Select...</option>
                {nodes.map((n) => (
                  <option key={n.id} value={n.id}>{n.id}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="text-[10px] text-text-muted">Iterations</label>
              <input
                type="number" min={100} max={50000} step={100}
                value={iterations}
                onChange={(e) => setIterations(+e.target.value)}
                className="w-full rounded-lg border border-border bg-bg-primary px-3 py-2 text-xs font-mono text-text-primary focus:border-accent focus:outline-none"
              />
            </div>
          </div>

          {/* Edge Updates */}
          <div>
            <p className="text-[10px] font-semibold text-text-muted uppercase mb-1">Update Edge Probability</p>
            <div className="flex gap-2">
              <select value={updateForm.from} onChange={(e) => setUpdateForm({ ...updateForm, from: e.target.value })} className="flex-1 rounded border border-border bg-bg-primary px-2 py-1.5 text-[10px] text-text-primary focus:outline-none">
                <option value="">From</option>
                {nodes.map((n) => <option key={n.id} value={n.id}>{n.id}</option>)}
              </select>
              <select value={updateForm.to} onChange={(e) => setUpdateForm({ ...updateForm, to: e.target.value })} className="flex-1 rounded border border-border bg-bg-primary px-2 py-1.5 text-[10px] text-text-primary focus:outline-none">
                <option value="">To</option>
                {nodes.map((n) => <option key={n.id} value={n.id}>{n.id}</option>)}
              </select>
              <input type="number" step="0.05" min="0" max="1" value={updateForm.newProbability} onChange={(e) => setUpdateForm({ ...updateForm, newProbability: +e.target.value })} className="w-16 rounded border border-border bg-bg-primary px-2 py-1.5 text-[10px] font-mono text-text-primary focus:outline-none" />
              <button
                onClick={() => {
                  if (updateForm.from && updateForm.to) {
                    setEdgeUpdates([...edgeUpdates, { ...updateForm }]);
                    setUpdateForm({ from: "", to: "", newProbability: 0.3 });
                  }
                }}
                className="rounded bg-accent/15 px-2 py-1 text-[10px] text-accent hover:bg-accent/25"
              >+</button>
            </div>
          </div>

          {/* Edge Removals */}
          <div>
            <p className="text-[10px] font-semibold text-text-muted uppercase mb-1">Remove Edge</p>
            <div className="flex gap-2">
              <select value={removeForm.from} onChange={(e) => setRemoveForm({ ...removeForm, from: e.target.value })} className="flex-1 rounded border border-border bg-bg-primary px-2 py-1.5 text-[10px] text-text-primary focus:outline-none">
                <option value="">From</option>
                {nodes.map((n) => <option key={n.id} value={n.id}>{n.id}</option>)}
              </select>
              <select value={removeForm.to} onChange={(e) => setRemoveForm({ ...removeForm, to: e.target.value })} className="flex-1 rounded border border-border bg-bg-primary px-2 py-1.5 text-[10px] text-text-primary focus:outline-none">
                <option value="">To</option>
                {nodes.map((n) => <option key={n.id} value={n.id}>{n.id}</option>)}
              </select>
              <button
                onClick={() => {
                  if (removeForm.from && removeForm.to) {
                    setEdgeRemovals([...edgeRemovals, { ...removeForm }]);
                    setRemoveForm({ from: "", to: "" });
                  }
                }}
                className="rounded bg-danger/15 px-2 py-1 text-[10px] text-danger hover:bg-danger/25"
              >+</button>
            </div>
          </div>

          {/* Pending modifications */}
          {(edgeUpdates.length + edgeRemovals.length + edgeAdditions.length) > 0 && (
            <div className="rounded-lg border border-border bg-bg-primary p-2">
              <div className="flex items-center justify-between mb-1">
                <p className="text-[10px] font-semibold text-text-muted">Pending Modifications</p>
                <button onClick={clearScenario} className="text-[10px] text-danger hover:underline">Clear all</button>
              </div>
              {edgeUpdates.map((u, i) => (
                <p key={`u${i}`} className="text-[10px] text-info">UPDATE {u.from}→{u.to} prob={u.newProbability}</p>
              ))}
              {edgeRemovals.map((r, i) => (
                <p key={`r${i}`} className="text-[10px] text-danger">REMOVE {r.from}→{r.to}</p>
              ))}
              {edgeAdditions.map((a, i) => (
                <p key={`a${i}`} className="text-[10px] text-success">ADD {a.from}→{a.to} prob={a.probability}</p>
              ))}
            </div>
          )}

          <button
            onClick={handleRun}
            disabled={!failedNode || loading}
            className="btn-primary !text-xs"
          >
            {loading ? "Simulating..." : "Run What-If Analysis"}
          </button>
        </div>

        {/* Right: Results */}
        <div className="flex flex-col gap-4">
          {whatIfResult ? (
            <>
              {/* Before/After comparison */}
              <div className="grid grid-cols-2 gap-3">
                <div className="rounded-xl border border-border bg-bg-card p-4 text-center">
                  <p className="text-xs text-text-muted mb-1">Before</p>
                  <p className="text-2xl font-bold text-text-secondary">{(whatIfResult.beforeResilience * 100).toFixed(1)}%</p>
                  <p className="text-[10px] text-text-muted">Resilience</p>
                </div>
                <div className="rounded-xl border border-border bg-bg-card p-4 text-center">
                  <p className="text-xs text-text-muted mb-1">After</p>
                  <p className={`text-2xl font-bold ${whatIfResult.resilienceDelta > 0 ? "text-success" : whatIfResult.resilienceDelta < 0 ? "text-danger" : "text-text-primary"}`}>
                    {(whatIfResult.afterResilience * 100).toFixed(1)}%
                  </p>
                  <p className="text-[10px] text-text-muted">Resilience</p>
                </div>
              </div>

              {/* Delta card */}
              <div className={`rounded-xl border p-4 text-center ${
                whatIfResult.resilienceDelta > 0 ? "border-success/30 bg-success/10" :
                whatIfResult.resilienceDelta < 0 ? "border-danger/30 bg-danger/10" :
                "border-border bg-bg-card"
              }`}>
                <p className={`text-xl font-bold ${whatIfResult.resilienceDelta > 0 ? "text-success" : whatIfResult.resilienceDelta < 0 ? "text-danger" : "text-text-primary"}`}>
                  {whatIfResult.resilienceDelta > 0 ? "↑" : whatIfResult.resilienceDelta < 0 ? "↓" : "→"}{" "}
                  {Math.abs(whatIfResult.resilienceDelta * 100).toFixed(2)}%
                </p>
                <p className="text-xs text-text-secondary mt-1">{whatIfResult.verdict}</p>
              </div>

              {/* Per-node comparison chart */}
              {comparisonData.length > 0 && (
                <div className="rounded-xl border border-border bg-bg-card p-4">
                  <p className="text-xs font-semibold text-text-primary mb-2">Per-Node Failure Probability</p>
                  <ResponsiveContainer width="100%" height={250}>
                    <BarChart data={comparisonData}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#27272a" />
                      <XAxis dataKey="node" tick={{ fill: "#a1a1aa", fontSize: 10 }} axisLine={{ stroke: "#27272a" }} tickLine={false} />
                      <YAxis tick={{ fill: "#a1a1aa", fontSize: 10 }} axisLine={{ stroke: "#27272a" }} tickLine={false} unit="%" />
                      <Tooltip contentStyle={{ background: "#18181b", border: "1px solid #27272a", borderRadius: "10px", fontSize: "12px", color: "#fafafa", boxShadow: "0 8px 32px rgba(0,0,0,0.5)" }} />
                      <Legend wrapperStyle={{ fontSize: "11px" }} />
                      <Bar dataKey="before" name="Before" fill="#52525b" radius={[3, 3, 0, 0]} />
                      <Bar dataKey="after" name="After" fill="#7c3aed" radius={[3, 3, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              )}
            </>
          ) : (
            <div className="flex h-full flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-bg-card p-12 text-center">
              <p className="text-text-muted text-sm">Configure modifications and run<br />to see before/after comparison</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
