import { useCallback, useEffect, useMemo } from "react";
import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
} from "reactflow";
import "reactflow/dist/style.css";
import { useGraphStore } from "../../store/graphStore";
import ServiceNodeCard from "./ServiceNode";
import DependencyEdge from "./DependencyEdge";

const nodeTypes = { service: ServiceNodeCard };
const edgeTypes = { dependency: DependencyEdge };

function layoutNodes(services) {
  const cols = Math.max(3, Math.ceil(Math.sqrt(services.length)));
  return services.map((svc, i) => ({
    id: svc.id,
    type: "service",
    position: { x: (i % cols) * 240 + 40, y: Math.floor(i / cols) * 180 + 40 },
    data: { ...svc },
  }));
}

function mapEdges(dependencies) {
  return dependencies.map((dep) => ({
    id: `${dep.from}->${dep.to}`,
    source: dep.from,
    target: dep.to,
    type: "dependency",
    data: {
      failureProbability: dep.failureProbability,
      infraCost: dep.infraCost,
      propagationDelay: dep.propagationDelay,
    },
  }));
}

export default function GraphCanvas() {
  const { nodes: graphNodes, edges: graphEdges, setSelectedNode, setSelectedEdge } = useGraphStore();

  const initialNodes = useMemo(() => layoutNodes(graphNodes), [graphNodes]);
  const initialEdges = useMemo(() => mapEdges(graphEdges), [graphEdges]);

  const [rfNodes, setRfNodes, onNodesChange] = useNodesState([]);
  const [rfEdges, setRfEdges, onEdgesChange] = useEdgesState([]);

  useEffect(() => {
    setRfNodes(layoutNodes(graphNodes));
  }, [graphNodes, setRfNodes]);

  useEffect(() => {
    setRfEdges(mapEdges(graphEdges));
  }, [graphEdges, setRfEdges]);

  const onNodeClick = useCallback(
    (_, node) => setSelectedNode(node.data),
    [setSelectedNode]
  );

  const onEdgeClick = useCallback(
    (_, edge) => setSelectedEdge(edge.data),
    [setSelectedEdge]
  );

  const onPaneClick = useCallback(() => {
    setSelectedNode(null);
    setSelectedEdge(null);
  }, [setSelectedNode, setSelectedEdge]);

  return (
    <div className="h-full w-full">
      <ReactFlow
        nodes={rfNodes}
        edges={rfEdges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onNodeClick={onNodeClick}
        onEdgeClick={onEdgeClick}
        onPaneClick={onPaneClick}
        nodeTypes={nodeTypes}
        edgeTypes={edgeTypes}
        fitView
        fitViewOptions={{ padding: 0.2 }}
        proOptions={{ hideAttribution: true }}
      >
        <Background color="#27272a" gap={20} size={1} />
        <Controls
          className="!bg-bg-card !border-border !text-text-secondary !rounded-lg !shadow-lg"
        />
        <MiniMap
          nodeColor={(n) => {
            const state = n.data?.state;
            if (state === "FAILED") return "#ef4444";
            if (state === "RECOVERING") return "#f59e0b";
            return "#10b981";
          }}
          className="!bg-bg-card !border-border !rounded-lg"
          maskColor="rgba(9, 9, 11, 0.7)"
        />
      </ReactFlow>
    </div>
  );
}
