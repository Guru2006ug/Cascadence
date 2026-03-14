import { useState } from "react";
import { architectureService } from "../services/architectureService";
import ArchitectureScoreCard from "../components/architecture/ArchitectureScoreCard";
import ArchitectureComparison from "../components/architecture/ArchitectureComparison";

export default function Architecture() {
  const [scoreResult, setScoreResult] = useState(null);
  const [comparisonResult, setComparisonResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [proposedJson, setProposedJson] = useState("");
  const [showCompare, setShowCompare] = useState(false);

  const fetchScore = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await architectureService.getScore();
      setScoreResult(res.data);
    } catch (err) {
      setError(err.response?.data?.error || "Failed to fetch architecture score");
    } finally {
      setLoading(false);
    }
  };

  const handleCompare = async () => {
    if (!proposedJson.trim()) return;
    setLoading(true);
    setError(null);
    try {
      const proposed = JSON.parse(proposedJson);
      const res = await architectureService.compareArchitecture(proposed);
      setComparisonResult(res.data);
    } catch (err) {
      if (err instanceof SyntaxError) {
        setError("Invalid JSON format for proposed graph");
      } else {
        setError(err.response?.data?.error || "Architecture comparison failed");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-8 overflow-y-auto p-10 max-w-5xl">
      <div className="animate-in">
        <h1 className="text-2xl font-bold tracking-tight">Architecture Intelligence</h1>
        <p className="mt-2 text-sm text-text-secondary">Composite scoring, grading & architecture comparison</p>
      </div>

      {error && <div className="error-banner"><span>{error}</span><button onClick={() => setError(null)} className="text-danger/60 hover:text-danger">✕</button></div>}

      <div className="flex gap-3">
        <button onClick={fetchScore} disabled={loading} className="btn-primary">
          {loading ? "Analyzing..." : "Analyze Architecture"}
        </button>
        <button onClick={() => setShowCompare(!showCompare)} className="btn-ghost">
          {showCompare ? "Hide Comparison" : "Compare Architecture"}
        </button>
      </div>

      {/* Score Card */}
      {scoreResult && <ArchitectureScoreCard scoreResult={scoreResult} />}

      {showCompare && (
        <div className="card flex flex-col gap-4">
          <p className="text-xs font-semibold text-text-primary">Proposed Graph (JSON)</p>
          <textarea value={proposedJson} onChange={(e) => setProposedJson(e.target.value)} rows={8} placeholder='{"services": [...], "dependencies": [...]}' className="input !font-mono !text-xs resize-y" />
          <button onClick={handleCompare} disabled={loading || !proposedJson.trim()} className="btn-primary self-start !text-xs">Compare</button>
        </div>
      )}
      {comparisonResult && <ArchitectureComparison comparisonResult={comparisonResult} />}

      {!scoreResult && !comparisonResult && !loading && (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-bg-card p-16 text-center">
          <p className="text-text-muted text-sm">Analyze your architecture to get a composite health score</p>
        </div>
      )}
    </div>
  );
}
