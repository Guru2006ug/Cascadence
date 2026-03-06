package com.klu.backend.algorithm;

import com.klu.backend.model.Edge;
import com.klu.backend.model.Graph;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Recovery planning algorithms:
 *   1. Cycle detection   — DFS coloring (WHITE → GRAY → BLACK)
 *   2. Topological sort  — Kahn's algorithm (BFS with inDegree)
 *   3. Longest path      — DP on topological order (critical chain)
 *
 * All operate on the forward dependency graph.
 * Complexity: O(V + E) for each operation.
 */
@Component
public class TopoSortUtil {

    // ──────────────────────────── Cycle Detection ────────────────────────────

    /**
     * Detects whether the dependency graph contains a cycle using DFS coloring.
     *
     * WHITE = unvisited, GRAY = in current DFS path, BLACK = fully explored.
     * If we encounter a GRAY node → back edge → cycle exists.
     *
     * @return true if graph has at least one cycle
     */
    public boolean hasCycle(Graph graph) {
        Set<String> white = new HashSet<>(graph.getNodes().keySet());
        Set<String> gray = new HashSet<>();
        Set<String> black = new HashSet<>();
        Map<String, List<Edge>> adj = graph.getAdjacencyList();

        for (String node : graph.getNodes().keySet()) {
            if (white.contains(node)) {
                if (dfsCycleDetect(node, adj, white, gray, black)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean dfsCycleDetect(String node, Map<String, List<Edge>> adj,
                                    Set<String> white, Set<String> gray, Set<String> black) {
        white.remove(node);
        gray.add(node);

        for (Edge edge : adj.getOrDefault(node, List.of())) {
            String neighbor = edge.getTo();
            if (gray.contains(neighbor)) {
                return true;  // Back edge found → cycle
            }
            if (white.contains(neighbor) && dfsCycleDetect(neighbor, adj, white, gray, black)) {
                return true;
            }
        }

        gray.remove(node);
        black.add(node);
        return false;
    }

    // ──────────────────────────── Topological Sort (Kahn's) ────────────────────────────

    /**
     * Computes topological order using Kahn's BFS algorithm.
     *
     * Process:
     *   1. Copy inDegree map (graph returns a safe copy)
     *   2. Enqueue all nodes with inDegree == 0
     *   3. Dequeue, add to order, decrement neighbors' inDegree
     *   4. If processed count != total nodes → cycle exists
     *
     * @return topological order and cycle flag
     */
    public TopologicalSortData topologicalSort(Graph graph) {
        Map<String, Integer> inDeg = graph.getInDegree();  // mutable copy
        Map<String, List<Edge>> adj = graph.getAdjacencyList();
        int totalNodes = graph.getNodeCount();

        Queue<String> queue = new LinkedList<>();
        List<String> order = new ArrayList<>();

        // Seed with zero-inDegree nodes
        for (Map.Entry<String, Integer> entry : inDeg.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            order.add(current);

            for (Edge edge : adj.getOrDefault(current, List.of())) {
                String neighbor = edge.getTo();
                inDeg.merge(neighbor, -1, Integer::sum);
                if (inDeg.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        boolean hasCycle = order.size() != totalNodes;
        return new TopologicalSortData(order, hasCycle);
    }

    // ──────────────────────────── Longest Path in DAG ────────────────────────────

    /**
     * Computes the longest path (critical chain length) in a DAG.
     *
     * Uses DP on topological order:
     *   dp[node] = max distance from any source to this node
     *   For each edge u → v: dp[v] = max(dp[v], dp[u] + 1)
     *
     * @return length of longest dependency chain, or -1 if cycle exists
     */
    public int longestPath(Graph graph) {
        TopologicalSortData sortData = topologicalSort(graph);
        if (sortData.hasCycle()) {
            return -1;
        }

        Map<String, List<Edge>> adj = graph.getAdjacencyList();
        Map<String, Integer> dp = new HashMap<>();

        for (String node : sortData.order()) {
            dp.put(node, 0);
        }

        int maxPath = 0;

        for (String node : sortData.order()) {
            for (Edge edge : adj.getOrDefault(node, List.of())) {
                int newDist = dp.get(node) + 1;
                if (newDist > dp.getOrDefault(edge.getTo(), 0)) {
                    dp.put(edge.getTo(), newDist);
                    maxPath = Math.max(maxPath, newDist);
                }
            }
        }

        return maxPath;
    }

    /**
     * Result of topological sort operation.
     */
    public record TopologicalSortData(List<String> order, boolean hasCycle) {}
}
