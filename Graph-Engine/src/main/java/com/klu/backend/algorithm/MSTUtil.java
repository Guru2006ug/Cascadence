package com.klu.backend.algorithm;

import com.klu.backend.model.Edge;
import com.klu.backend.model.Graph;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Minimum Spanning Tree computation using Kruskal's algorithm.
 *
 * Used for infrastructure cost optimization:
 *   - Treats the directed graph as undirected
 *   - Uses infraCost as edge weight
 *   - Finds the minimum-cost spanning tree
 *
 * Internally uses DSU for cycle detection.
 *
 * Complexity: O(E log E)
 */
@Component
public class MSTUtil {

    /**
     * Compute MST using Kruskal's algorithm.
     *
     * Steps:
     *   1. Collect all edges, sort by infraCost ascending
     *   2. Initialize DSU with all nodes
     *   3. Greedily add edges that don't create a cycle
     *   4. Stop when we have V-1 edges
     *
     * @param graph the service dependency graph
     * @return MST edges and total infrastructure cost
     */
    public MSTData computeMST(Graph graph) {
        List<Edge> allEdges = graph.getAllEdges();
        Set<String> nodeSet = graph.getNodes().keySet();

        if (nodeSet.isEmpty()) {
            return new MSTData(List.of(), 0.0);
        }

        // Sort edges by infrastructure cost
        allEdges.sort(Comparator.comparingDouble(Edge::getInfraCost));

        DSU dsu = new DSU(nodeSet);
        List<Edge> mstEdges = new ArrayList<>();
        double totalCost = 0.0;

        for (Edge edge : allEdges) {
            if (!dsu.connected(edge.getFrom(), edge.getTo())) {
                dsu.union(edge.getFrom(), edge.getTo());
                mstEdges.add(edge);
                totalCost += edge.getInfraCost();
            }
            // MST has at most V-1 edges
            if (mstEdges.size() == nodeSet.size() - 1) break;
        }

        return new MSTData(mstEdges, totalCost);
    }

    /**
     * Result of MST computation.
     */
    public record MSTData(List<Edge> edges, double totalCost) {}
}
