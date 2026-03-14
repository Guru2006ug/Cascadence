const priorityStyles = {
  HIGH: "border-danger/30 bg-danger/10 text-danger",
  MEDIUM: "border-warning/30 bg-warning/10 text-warning",
  LOW: "border-info/30 bg-info/10 text-info",
};

const typeIcons = {
  SPOF_ELIMINATION: "🎯",
  EDGE_HARDENING: "🛡",
  REDUNDANCY: "🔄",
  CLUSTER_ISOLATION: "🧩",
  COST_OPTIMIZATION: "💰",
};

export default function RecommendationList({ recommendationResult }) {
  if (!recommendationResult) return null;

  const { recommendations, totalRecommendations, countByPriority, countByType } = recommendationResult;

  return (
    <div className="flex flex-col gap-4">
      {/* Summary */}
      <div className="flex items-center gap-4">
        <span className="rounded-xl border border-border bg-bg-card px-4 py-2 text-sm font-bold text-text-primary">
          {totalRecommendations} Recommendations
        </span>
        {countByPriority && Object.entries(countByPriority).map(([priority, count]) => (
          <span
            key={priority}
            className={`rounded-full border px-3 py-1 text-xs font-semibold ${priorityStyles[priority] || ""}`}
          >
            {priority}: {count}
          </span>
        ))}
      </div>

      {/* List */}
      <div className="space-y-3">
        {recommendations?.map((rec, idx) => (
          <div
            key={idx}
            className={`rounded-xl border p-4 transition-colors hover:bg-bg-hover ${
              rec.priority === "HIGH" ? "border-danger/20 bg-danger/5" :
              rec.priority === "MEDIUM" ? "border-warning/20 bg-warning/5" :
              "border-border bg-bg-card"
            }`}
          >
            <div className="flex items-start gap-3">
              <span className="text-xl shrink-0">{typeIcons[rec.type] || "📋"}</span>
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <span className={`text-[10px] font-bold uppercase rounded-full px-2 py-0.5 ${priorityStyles[rec.priority]}`}>
                    {rec.priority}
                  </span>
                  <span className="text-[10px] font-mono text-text-muted">{rec.type}</span>
                </div>
                <p className="text-sm text-text-primary font-medium">{rec.description}</p>
                <div className="mt-1.5 flex items-center gap-2">
                  <span className="text-[10px] text-text-muted">Target:</span>
                  <span className="text-xs font-mono text-accent">{rec.target}</span>
                </div>
                {rec.expectedImpact && (
                  <p className="mt-1 text-[11px] text-text-secondary">Impact: {rec.expectedImpact}</p>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
