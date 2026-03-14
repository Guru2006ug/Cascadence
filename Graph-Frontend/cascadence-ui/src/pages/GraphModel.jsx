import { useEffect, useState } from "react";
import { useGraphStore } from "../store/graphStore";
import GraphCanvas from "../components/graph/GraphCanvas";
import { Download, Trash2 } from "lucide-react";

const SAMPLE_GRAPH = {
  services: [
    { id: "api-gateway", restartCost: 5, recoveryTime: 3, importanceWeight: 5 },
    { id: "auth-service", restartCost: 4, recoveryTime: 2, importanceWeight: 4 },
    { id: "user-service", restartCost: 3, recoveryTime: 2, importanceWeight: 3 },
    { id: "payment-service", restartCost: 8, recoveryTime: 5, importanceWeight: 5 },
    { id: "order-service", restartCost: 6, recoveryTime: 4, importanceWeight: 4 },
    { id: "notification", restartCost: 2, recoveryTime: 1, importanceWeight: 2 },
    { id: "database", restartCost: 10, recoveryTime: 8, importanceWeight: 5 },
  ],
  dependencies: [
    { from: "api-gateway", to: "auth-service", failureProbability: 0.7, infraCost: 2, propagationDelay: 1 },
    { from: "api-gateway", to: "user-service", failureProbability: 0.5, infraCost: 1.5, propagationDelay: 0.5 },
    { from: "auth-service", to: "database", failureProbability: 0.8, infraCost: 3, propagationDelay: 2 },
    { from: "user-service", to: "database", failureProbability: 0.6, infraCost: 2, propagationDelay: 1.5 },
    { from: "order-service", to: "payment-service", failureProbability: 0.9, infraCost: 4, propagationDelay: 1 },
    { from: "order-service", to: "database", failureProbability: 0.7, infraCost: 3, propagationDelay: 2 },
    { from: "payment-service", to: "notification", failureProbability: 0.4, infraCost: 1, propagationDelay: 0.5 },
  ],
};

export default function GraphModel() {
  const {
    fetchGraph, addService, removeService, addDependency, removeDependency,
    loadGraph, clearGraph, nodes, edges, serviceCount, dependencyCount,
    selectedNode, selectedEdge, clearSelection, error, clearError, loading,
  } = useGraphStore();

  const [tab, setTab] = useState("service");
  const [svcForm, setSvcForm] = useState({ id: "", restartCost: 5, recoveryTime: 2, importanceWeight: 3 });
  const [depForm, setDepForm] = useState({ from: "", to: "", failureProbability: 0.5, infraCost: 1, propagationDelay: 0 });

  useEffect(() => { fetchGraph(); }, [fetchGraph]);

  const handleAddService = async (e) => {
    e.preventDefault();
    if (!svcForm.id.trim()) return;
    await addService(svcForm);
    setSvcForm({ id: "", restartCost: 5, recoveryTime: 2, importanceWeight: 3 });
  };

  const handleAddDependency = async (e) => {
    e.preventDefault();
    if (!depForm.from || !depForm.to) return;
    await addDependency(depForm);
    setDepForm({ from: "", to: "", failureProbability: 0.5, infraCost: 1, propagationDelay: 0 });
  };

  return (
    <div className="flex h-full">
      {/* Canvas */}
      <div className="flex-1 relative">
        <div className="absolute top-4 left-4 z-10 flex items-center gap-2">
          <span className="rounded-lg bg-bg-card border border-border px-3 py-2 text-xs font-mono text-text-secondary">
            {serviceCount} services · {dependencyCount} deps
          </span>
          <button onClick={() => loadGraph(SAMPLE_GRAPH)} className="btn-ghost !py-2 !px-3 !text-xs">
            <Download className="h-3.5 w-3.5" /> Sample
          </button>
          <button onClick={clearGraph} className="btn-danger !py-2 !px-3 !text-xs">
            <Trash2 className="h-3.5 w-3.5" /> Clear
          </button>
        </div>
        <GraphCanvas />
      </div>

      {/* Side Panel */}
      <div className="w-80 shrink-0 border-l border-border bg-bg-secondary flex flex-col overflow-y-auto">
        {error && (
          <div className="error-banner mx-4 mt-4">
            <span>{error}</span>
            <button onClick={clearError} className="text-danger/60 hover:text-danger text-sm">✕</button>
          </div>
        )}

        {/* Tabs */}
        <div className="flex border-b border-border">
          {["service", "dependency"].map((t) => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`flex-1 py-3.5 text-xs font-medium transition-colors ${
                tab === t
                  ? "border-b-2 border-text-primary text-text-primary"
                  : "text-text-muted hover:text-text-secondary"
              }`}
            >
              {t === "service" ? "Service" : "Dependency"}
            </button>
          ))}
        </div>

        <div className="flex-1 p-5">
          {tab === "service" ? (
            <form onSubmit={handleAddService} className="flex flex-col gap-4">
              <p className="section-label">Add Service</p>
              <input type="text" placeholder="Service ID" value={svcForm.id} onChange={(e) => setSvcForm({ ...svcForm, id: e.target.value })} className="input" />
              <div className="grid grid-cols-3 gap-2">
                <div>
                  <label className="section-label block mb-1.5">Cost</label>
                  <input type="number" step="0.1" min="0" value={svcForm.restartCost} onChange={(e) => setSvcForm({ ...svcForm, restartCost: +e.target.value })} className="input !text-sm !font-mono" />
                </div>
                <div>
                  <label className="section-label block mb-1.5">Recovery</label>
                  <input type="number" step="0.1" min="0" value={svcForm.recoveryTime} onChange={(e) => setSvcForm({ ...svcForm, recoveryTime: +e.target.value })} className="input !text-sm !font-mono" />
                </div>
                <div>
                  <label className="section-label block mb-1.5">Weight</label>
                  <input type="number" step="0.1" min="0" value={svcForm.importanceWeight} onChange={(e) => setSvcForm({ ...svcForm, importanceWeight: +e.target.value })} className="input !text-sm !font-mono" />
                </div>
              </div>
              <button type="submit" disabled={loading} className="btn-primary">
                {loading ? "Adding..." : "Add Service"}
              </button>
            </form>
          ) : (
            <form onSubmit={handleAddDependency} className="flex flex-col gap-4">
              <p className="section-label">Add Dependency</p>
              <select value={depForm.from} onChange={(e) => setDepForm({ ...depForm, from: e.target.value })} className="input">
                <option value="">From...</option>
                {nodes.map((n) => (<option key={n.id} value={n.id}>{n.id}</option>))}
              </select>
              <select value={depForm.to} onChange={(e) => setDepForm({ ...depForm, to: e.target.value })} className="input">
                <option value="">To...</option>
                {nodes.map((n) => (<option key={n.id} value={n.id}>{n.id}</option>))}
              </select>
              <div className="grid grid-cols-3 gap-2">
                <div>
                  <label className="section-label block mb-1.5">Prob</label>
                  <input type="number" step="0.05" min="0" max="1" value={depForm.failureProbability} onChange={(e) => setDepForm({ ...depForm, failureProbability: +e.target.value })} className="input !text-sm !font-mono" />
                </div>
                <div>
                  <label className="section-label block mb-1.5">Cost</label>
                  <input type="number" step="0.1" min="0" value={depForm.infraCost} onChange={(e) => setDepForm({ ...depForm, infraCost: +e.target.value })} className="input !text-sm !font-mono" />
                </div>
                <div>
                  <label className="section-label block mb-1.5">Delay</label>
                  <input type="number" step="0.1" min="0" value={depForm.propagationDelay} onChange={(e) => setDepForm({ ...depForm, propagationDelay: +e.target.value })} className="input !text-sm !font-mono" />
                </div>
              </div>
              <button type="submit" disabled={loading} className="btn-primary">
                {loading ? "Adding..." : "Add Dependency"}
              </button>
            </form>
          )}

          {/* Selected Node Detail */}
          {selectedNode && (
            <div className="mt-6 card">
              <div className="flex items-center justify-between">
                <p className="text-sm font-medium text-text-primary">{selectedNode.id}</p>
                <span className={`badge ${
                  selectedNode.state === "HEALTHY" ? "bg-success-dim text-success" :
                  selectedNode.state === "FAILED" ? "bg-danger-dim text-danger" :
                  "bg-warning-dim text-warning"
                }`}>{selectedNode.state}</span>
              </div>
              <div className="mt-3 grid grid-cols-3 gap-2 text-xs text-text-muted">
                <div>Cost <span className="font-mono text-text-secondary block">{selectedNode.restartCost}</span></div>
                <div>Recovery <span className="font-mono text-text-secondary block">{selectedNode.recoveryTime}s</span></div>
                <div>Weight <span className="font-mono text-text-secondary block">{selectedNode.importanceWeight}</span></div>
              </div>
              <button onClick={() => removeService(selectedNode.id)} className="btn-danger mt-4 w-full !text-xs">
                Remove Service
              </button>
            </div>
          )}

          {/* Selected Edge Detail */}
          {selectedEdge && (
            <div className="mt-6 card">
              <p className="text-sm font-medium text-text-primary">
                {selectedEdge.from || "?"} → {selectedEdge.to || "?"}
              </p>
              <div className="mt-3 grid grid-cols-3 gap-2 text-xs text-text-muted">
                <div>Prob <span className="font-mono text-text-secondary block">{selectedEdge.failureProbability}</span></div>
                <div>Cost <span className="font-mono text-text-secondary block">{selectedEdge.infraCost}</span></div>
                <div>Delay <span className="font-mono text-text-secondary block">{selectedEdge.propagationDelay}s</span></div>
              </div>
              <button onClick={() => removeDependency(selectedEdge.from, selectedEdge.to)} className="btn-danger mt-4 w-full !text-xs">
                Remove Dependency
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
