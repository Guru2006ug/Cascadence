import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";

export default function SensitivityChart({ sensitivityData }) {
  if (!sensitivityData?.rankings?.length) return null;

  const data = sensitivityData.rankings.slice(0, 10).map((edge) => ({
    edge: `${edge.from}→${edge.to}`,
    riskReduction: +(edge.riskReduction * 100).toFixed(2),
    originalProbability: +(edge.originalProbability * 100).toFixed(1),
  }));

  return (
    <div className="card">
      <p className="text-xs font-semibold text-text-primary mb-1">Edge Sensitivity (Hardening Impact)</p>
      <p className="text-[10px] text-text-muted mb-3">
        Top edges where halving probability reduces system fragility most
      </p>
      <ResponsiveContainer width="100%" height={280}>
        <BarChart data={data} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#27272a" />
          <XAxis dataKey="edge" tick={{ fill: "#a1a1aa", fontSize: 9 }} axisLine={{ stroke: "#27272a" }} tickLine={false} interval={0} angle={-20} textAnchor="end" height={50} />
          <YAxis tick={{ fill: "#a1a1aa", fontSize: 10 }} axisLine={{ stroke: "#27272a" }} tickLine={false} unit="%" />
          <Tooltip contentStyle={{ background: "#18181b", border: "1px solid #27272a", borderRadius: "10px", fontSize: "12px", color: "#fafafa", boxShadow: "0 8px 32px rgba(0,0,0,0.4)" }} />
          <Legend wrapperStyle={{ fontSize: "10px", color: "#a1a1aa" }} />
          <Bar dataKey="riskReduction" name="Risk Reduction %" fill="#7c3aed" radius={[4, 4, 0, 0]} />
          <Bar dataKey="originalProbability" name="Original Prob %" fill="#3f3f46" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>

      {sensitivityData.mostSensitive && (
        <div className="mt-3 rounded-lg bg-accent/10 border border-accent/20 px-3 py-2 text-xs">
          <span className="text-accent font-semibold">Most Sensitive: </span>
          <span className="text-text-secondary">
            {sensitivityData.mostSensitive.from} → {sensitivityData.mostSensitive.to}
            {" "}(reduction: {(sensitivityData.mostSensitive.riskReduction * 100).toFixed(2)}%)
          </span>
        </div>
      )}
    </div>
  );
}
