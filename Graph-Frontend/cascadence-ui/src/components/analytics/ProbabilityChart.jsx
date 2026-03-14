import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from "recharts";

export default function ProbabilityChart({ failureProbabilities }) {
  if (!failureProbabilities) return null;

  const data = Object.entries(failureProbabilities)
    .map(([node, prob]) => ({ node, probability: +(prob * 100).toFixed(1) }))
    .sort((a, b) => b.probability - a.probability);

  const getColor = (prob) => {
    if (prob >= 70) return "#ef4444";
    if (prob >= 40) return "#f59e0b";
    return "#10b981";
  };

  return (
    <div className="card">
      <p className="text-xs font-semibold text-text-primary mb-3">Failure Probability per Node</p>
      <ResponsiveContainer width="100%" height={280}>
        <BarChart data={data} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#27272a" />
          <XAxis dataKey="node" tick={{ fill: "#a1a1aa", fontSize: 10 }} axisLine={{ stroke: "#27272a" }} tickLine={false} />
          <YAxis tick={{ fill: "#a1a1aa", fontSize: 10 }} axisLine={{ stroke: "#27272a" }} tickLine={false} domain={[0, 100]} unit="%" />
          <Tooltip contentStyle={{ background: "#18181b", border: "1px solid #27272a", borderRadius: "10px", fontSize: "12px", color: "#fafafa", boxShadow: "0 8px 32px rgba(0,0,0,0.4)" }} formatter={(val) => [`${val}%`, "Probability"]} />
          <Bar dataKey="probability" radius={[4, 4, 0, 0]}>
            {data.map((entry, i) => (<Cell key={i} fill={getColor(entry.probability)} />))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
