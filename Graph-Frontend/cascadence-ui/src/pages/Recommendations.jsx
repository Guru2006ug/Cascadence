import { useState } from "react";
import { recommendationService } from "../services/recommendationService";
import RecommendationList from "../components/recommendation/RecommendationList";

export default function Recommendations() {
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchRecommendations = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await recommendationService.getRecommendations();
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.error || "Failed to fetch recommendations");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-8 overflow-y-auto p-10 max-w-5xl">
      <div className="animate-in">
        <h1 className="text-2xl font-bold tracking-tight">Recommendations</h1>
        <p className="mt-2 text-sm text-text-secondary">Prioritized improvement suggestions for your architecture</p>
      </div>

      {error && <div className="error-banner"><span>{error}</span><button onClick={() => setError(null)} className="text-danger/60 hover:text-danger">✕</button></div>}

      <button onClick={fetchRecommendations} disabled={loading} className="btn-primary self-start">
        {loading ? "Analyzing..." : "Generate Recommendations"}
      </button>

      {result && <RecommendationList recommendationResult={result} />}

      {!result && !loading && (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-bg-card p-16 text-center">
          <p className="text-text-muted text-sm">Generate recommendations to improve your architecture</p>
        </div>
      )}
    </div>
  );
}
