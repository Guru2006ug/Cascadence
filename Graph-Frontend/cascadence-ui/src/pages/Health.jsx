import { useState, useEffect } from "react";
import { useGraphStore } from "../store/graphStore";
import { healthService } from "../services/healthService";
import HealthDashboardPanel from "../components/health/HealthDashboardPanel";

export default function Health() {
  const { nodes, fetchGraph } = useGraphStore();
  const [dashboard, setDashboard] = useState(null);
  const [recoveryPlan, setRecoveryPlan] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [stateForm, setStateForm] = useState({ nodeId: "", state: "HEALTHY" });

  useEffect(() => { fetchGraph(); }, [fetchGraph]);

  const fetchDashboard = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await healthService.getDashboard();
      setDashboard(res.data);
    } catch (err) {
      setError(err.response?.data?.error || "Failed to fetch health dashboard");
    } finally {
      setLoading(false);
    }
  };

  const handleStateUpdate = async (e) => {
    e.preventDefault();
    if (!stateForm.nodeId) return;
    setLoading(true);
    setError(null);
    try {
      await healthService.updateNodeState(stateForm.nodeId, stateForm.state);
      await fetchDashboard();
      await fetchGraph();
    } catch (err) {
      setError(err.response?.data?.error || "Failed to update state");
    } finally {
      setLoading(false);
    }
  };

  const fetchRecovery = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await healthService.getRecoveryPlan();
      setRecoveryPlan(res.data);
    } catch (err) {
      setError(err.response?.data?.error || "Failed to generate recovery plan");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-8 overflow-y-auto p-10 max-w-5xl">
      <div className="animate-in">
        <h1 className="text-2xl font-bold tracking-tight">Health Monitor</h1>
        <p className="mt-2 text-sm text-text-secondary">Live node states, cascade risk zones & recovery planning</p>
      </div>

      {error && <div className="error-banner"><span>{error}</span><button onClick={() => setError(null)} className="text-danger/60 hover:text-danger">✕</button></div>}

      <div className="card flex flex-wrap items-end gap-4">
        <button onClick={fetchDashboard} disabled={loading} className="btn-primary">
          {loading ? "Loading..." : "Refresh Dashboard"}
        </button>
        <button onClick={fetchRecovery} disabled={loading} className="btn-ghost">Recovery Plan</button>

        <form onSubmit={handleStateUpdate} className="flex items-end gap-2 ml-auto">
          <div>
            <label className="section-label block mb-1.5">Node</label>
            <select value={stateForm.nodeId} onChange={(e) => setStateForm({...stateForm, nodeId: e.target.value})} className="input !py-2.5">
              <option value="">Select...</option>
              {nodes.map((n) => (<option key={n.id} value={n.id}>{n.id}</option>))}
            </select>
          </div>
          <div>
            <label className="section-label block mb-1.5">State</label>
            <select value={stateForm.state} onChange={(e) => setStateForm({...stateForm, state: e.target.value})} className="input !py-2.5">
              <option value="HEALTHY">HEALTHY</option>
              <option value="FAILED">FAILED</option>
              <option value="RECOVERING">RECOVERING</option>
            </select>
          </div>
          <button type="submit" disabled={!stateForm.nodeId||loading} className="btn-ghost">Update</button>
        </form>
      </div>

      {/* Dashboard */}
      {dashboard && <HealthDashboardPanel dashboard={dashboard} />}

      {/* Recovery Plan */}
      {recoveryPlan && (
        <div className="rounded-xl border border-border bg-bg-card p-4">
          <div className="flex items-center justify-between mb-3">
            <p className="text-xs font-semibold text-text-primary">Recovery Plan</p>
            <div className="flex gap-3 text-[10px] text-text-muted">
              <span>Cost: {recoveryPlan.totalEstimatedCost?.toFixed(1)}</span>
              <span>Steps: {recoveryPlan.recoveryDepth}</span>
              {recoveryPlan.hasCyclicDependencies && (
                <span className="text-danger font-semibold">⚠ Cyclic Dependencies</span>
              )}
            </div>
          </div>
          <div className="space-y-2">
            {recoveryPlan.steps?.map((step) => (
              <div key={step.order} className="flex items-center gap-3 rounded-lg border border-border bg-bg-primary p-3">
                <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-accent/15 text-xs font-bold text-accent">
                  {step.order}
                </span>
                <div className="flex-1">
                  <span className="text-xs font-semibold text-text-primary">{step.nodeId}</span>
                  <span className="ml-2 text-[10px] font-mono text-text-muted">cost: {step.restartCost}</span>
                </div>
                {step.prerequisiteNodes?.length > 0 && (
                  <span className="text-[10px] text-text-muted">
                    after: {step.prerequisiteNodes.join(", ")}
                  </span>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {!dashboard && !loading && (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-bg-card p-16 text-center">
          <p className="text-text-muted text-sm">Load the dashboard to see live system health</p>
        </div>
      )}
    </div>
  );
}
