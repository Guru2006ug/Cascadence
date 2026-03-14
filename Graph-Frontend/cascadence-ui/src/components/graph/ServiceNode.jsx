import { Handle, Position } from "reactflow";
import { useGraphStore } from "../../store/graphStore";

const stateColors = {
  HEALTHY: { bg: "bg-success/15", border: "border-success/50", dot: "bg-success" },
  FAILED: { bg: "bg-danger/15", border: "border-danger/50", dot: "bg-danger" },
  RECOVERING: { bg: "bg-warning/15", border: "border-warning/50", dot: "bg-warning" },
};

export default function ServiceNodeCard({ data, selected }) {
  const setSelectedNode = useGraphStore((s) => s.setSelectedNode);
  const colors = stateColors[data.state] || stateColors.HEALTHY;

  return (
    <div
      onClick={() => setSelectedNode(data)}
      className={`min-w-[160px] cursor-pointer rounded-xl border ${colors.border} ${colors.bg} px-4 py-3 shadow-lg backdrop-blur-sm transition-all ${
        selected ? "ring-2 ring-accent ring-offset-1 ring-offset-bg-primary" : ""
      }`}
    >
      <Handle type="target" position={Position.Top} className="!bg-accent !w-2 !h-2" />

      {/* Header */}
      <div className="flex items-center justify-between gap-2">
        <span className="text-sm font-bold text-text-primary truncate">{data.id}</span>
        <span className={`h-2.5 w-2.5 rounded-full ${colors.dot}`} title={data.state} />
      </div>

      {/* Stats */}
      <div className="mt-2 grid grid-cols-2 gap-x-3 gap-y-1 text-[10px] text-text-muted">
        <span>Cost</span>
        <span className="text-right font-mono text-text-secondary">{data.restartCost}</span>
        <span>Recovery</span>
        <span className="text-right font-mono text-text-secondary">{data.recoveryTime}s</span>
        <span>Weight</span>
        <span className="text-right font-mono text-text-secondary">{data.importanceWeight}</span>
      </div>

      <Handle type="source" position={Position.Bottom} className="!bg-accent !w-2 !h-2" />
    </div>
  );
}
