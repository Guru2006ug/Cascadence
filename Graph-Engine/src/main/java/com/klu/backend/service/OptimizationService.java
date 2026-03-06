package com.klu.backend.service;

import com.klu.backend.algorithm.DSU;
import com.klu.backend.algorithm.DijkstraUtil;
import com.klu.backend.algorithm.MSTUtil;
import com.klu.backend.dto.response.ClusterResult;
import com.klu.backend.dto.response.DijkstraResult;
import com.klu.backend.dto.response.MSTResult;
import com.klu.backend.exception.GraphValidationException;
import com.klu.backend.model.Edge;
import com.klu.backend.model.Graph;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles cost and infrastructure optimization:
 *   - Dijkstra: minimum-cost recovery path
 *   - DSU: cluster detection (connected components)
 *   - MST (Kruskal): infrastructure topology optimization
 */
@Service
public class OptimizationService {

    private final GraphService graphService;
    private final DijkstraUtil dijkstraUtil;
    private final MSTUtil mstUtil;

    public OptimizationService(GraphService graphService,
                                DijkstraUtil dijkstraUtil,
                                MSTUtil mstUtil) {
        this.graphService = graphService;
        this.dijkstraUtil = dijkstraUtil;
        this.mstUtil = mstUtil;
    }

    // ──────────────────────────── Dijkstra ────────────────────────────

    /**
     * Find minimum-cost recovery paths from a source service.
     * Unreachable nodes are marked with cost = -1.
     */
    public DijkstraResult findMinCostRecovery(String source) {
        Graph graph = graphService.getGraph();

        if (!graph.containsNode(source)) {
            throw new GraphValidationException("Service not found: " + source);
        }

        DijkstraUtil.DijkstraData data = dijkstraUtil.findMinCostRecovery(source, graph);

        // Calculate total cost (only reachable nodes)
        double totalCost = data.costs().values().stream()
                .filter(c -> c < Double.MAX_VALUE)
                .mapToDouble(Double::doubleValue)
                .sum();

        // Replace MAX_VALUE with -1 for unreachable nodes
        Map<String, Double> cleanCosts = data.costs().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() == Double.MAX_VALUE ? -1.0 : e.getValue()
                ));

        return new DijkstraResult(data.source(), cleanCosts, data.paths(), totalCost);
    }

    // ──────────────────────────── Cluster Detection (DSU) ────────────────────────────

    /**
     * Identify independent service clusters using Disjoint Set Union.
     * Treats edges as undirected for connectivity analysis.
     */
    public ClusterResult detectClusters() {
        Graph graph = graphService.getGraph();

        if (graph.getNodeCount() == 0) {
            return new ClusterResult(List.of(), 0);
        }

        // Create a fresh DSU for this request
        DSU dsu = new DSU(graph.getNodes().keySet());

        // Union all connected services (undirected)
        for (Edge edge : graph.getAllEdges()) {
            dsu.union(edge.getFrom(), edge.getTo());
        }

        List<List<String>> clusters = dsu.getClusters();
        return new ClusterResult(clusters, clusters.size());
    }

    // ──────────────────────────── MST (Kruskal) ────────────────────────────

    /**
     * Compute minimum spanning tree for infrastructure cost optimization.
     */
    public MSTResult computeMST() {
        Graph graph = graphService.getGraph();
        MSTUtil.MSTData data = mstUtil.computeMST(graph);

        List<MSTResult.MSTEdgeInfo> edges = data.edges().stream()
                .map(e -> new MSTResult.MSTEdgeInfo(e.getFrom(), e.getTo(), e.getInfraCost()))
                .collect(Collectors.toList());

        return new MSTResult(edges, data.totalCost(), edges.size());
    }
}
