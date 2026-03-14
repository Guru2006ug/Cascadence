import { BaseEdge, EdgeLabelRenderer, getBezierPath } from "reactflow";

export default function DependencyEdge({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  data,
  selected,
}) {
  const [edgePath, labelX, labelY] = getBezierPath({
    sourceX,
    sourceY,
    targetX,
    targetY,
    sourcePosition,
    targetPosition,
  });

  const prob = data?.failureProbability ?? 1;
  const strokeColor = prob >= 0.7 ? "#ef4444" : prob >= 0.4 ? "#f59e0b" : "#10b981";

  return (
    <>
      <BaseEdge
        id={id}
        path={edgePath}
        style={{
          stroke: selected ? "#7c3aed" : strokeColor,
          strokeWidth: selected ? 2.5 : 1.8,
          opacity: 0.8,
        }}
      />
      <EdgeLabelRenderer>
        <div
          className="nodrag nopan pointer-events-auto absolute rounded-md bg-bg-card/90 px-1.5 py-0.5 text-[9px] font-mono text-text-secondary backdrop-blur-sm border border-border"
          style={{
            transform: `translate(-50%, -50%) translate(${labelX}px, ${labelY}px)`,
          }}
        >
          p={prob} {data?.propagationDelay > 0 && `• ${data.propagationDelay}s`}
        </div>
      </EdgeLabelRenderer>
    </>
  );
}
