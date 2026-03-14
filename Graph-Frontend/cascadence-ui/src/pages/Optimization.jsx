import { useEffect } from "react";
import { useGraphStore } from "../store/graphStore";
import DijkstraPanel from "../components/optimization/DijkstraPanel";
import ClusterPanel from "../components/optimization/ClusterPanel";
import MSTPanel from "../components/optimization/MSTPanel";

export default function Optimization() {
  const { fetchGraph } = useGraphStore();
  useEffect(() => { fetchGraph(); }, [fetchGraph]);

  return (
    <div className="flex flex-col gap-8 overflow-y-auto p-10 max-w-5xl">
      <div className="animate-in">
        <h1 className="text-2xl font-bold tracking-tight">Optimization & Performance</h1>
        <p className="mt-2 text-sm text-text-secondary">Graph algorithms — shortest paths, clusters, minimum spanning tree</p>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <DijkstraPanel />
        <ClusterPanel />
      </div>

      <MSTPanel />
    </div>
  );
}
