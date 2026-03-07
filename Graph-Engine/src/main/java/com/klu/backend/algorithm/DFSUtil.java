package com.klu.backend.algorithm;

import com.klu.backend.model.Edge;
import com.klu.backend.model.Graph;
import com.klu.backend.model.ServiceNode;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Deterministic cascading failure simulation using BFS on the reverse graph.
 *
 * Why reverse graph?
 *   Edge A → B means "A depends on B".
 *   If B fails, all nodes that depend on B (reachable via reverse edges) are affected.
 *   BFS gives us level-by-level cascade depth tracking.
 *
 * Also computes weighted impact score using ServiceNode.importanceWeight.
 *
 * Complexity: O(V + E)
 */
@Component
public class DFSUtil {

    /**
     * Simulate deterministic cascade from a failed node.
     *
     * @param failedNode the initially failed service
     * @param graph      the service dependency graph
     * @return cascade data including affected nodes, depth map, impact, and weighted impact
     */
    public CascadeData simulateCascade(String failedNode, Graph graph) {
        Map<String, List<Edge>> reverseAdj = graph.getReverseAdjList();
        Map<String, ServiceNode> nodes = graph.getNodes();

        Set<String> affected = new LinkedHashSet<>();
        Map<String, Integer> depthMap = new LinkedHashMap<>();
        Queue<String> queue = new LinkedList<>();

        // Seed the BFS with the initially failed node
        queue.add(failedNode);
        affected.add(failedNode);
        depthMap.put(failedNode, 0);

        int maxDepth = 0;

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentDepth = depthMap.get(current);

            // Find all services that depend on 'current' (reverse edges)
            for (Edge edge : reverseAdj.getOrDefault(current, List.of())) {
                String dependent = edge.getFrom();
                if (!affected.contains(dependent)) {
                    affected.add(dependent);
                    int newDepth = currentDepth + 1;
                    depthMap.put(dependent, newDepth);
                    maxDepth = Math.max(maxDepth, newDepth);
                    queue.add(dependent);
                }
            }
        }

        double impactScore = graph.getNodeCount() > 0
                ? (double) affected.size() / graph.getNodeCount()
                : 0.0;

        // Weighted impact: sum of importanceWeights of affected / total weight
        double totalWeight = nodes.values().stream()
                .mapToDouble(ServiceNode::getImportanceWeight).sum();
        double affectedWeight = affected.stream()
                .mapToDouble(id -> nodes.get(id).getImportanceWeight()).sum();
        double weightedImpactScore = totalWeight > 0 ? affectedWeight / totalWeight : 0.0;

        return new CascadeData(
                failedNode,
                new ArrayList<>(affected),
                depthMap,
                maxDepth,
                impactScore,
                weightedImpactScore,
                affected.size()
        );
    }

    /**
     * Internal data holder for cascade simulation results.
     */
    public record CascadeData(
            String failedNode,
            List<String> affectedNodes,
            Map<String, Integer> depthMap,
            int cascadeDepth,
            double impactScore,
            double weightedImpactScore,
            int affectedCount
    ) {}
}
