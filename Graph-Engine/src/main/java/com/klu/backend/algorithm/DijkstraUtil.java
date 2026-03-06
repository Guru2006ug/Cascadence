package com.klu.backend.algorithm;

import com.klu.backend.model.Edge;
import com.klu.backend.model.Graph;
import com.klu.backend.model.ServiceNode;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Cost-optimized recovery path computation using Dijkstra's algorithm.
 *
 * Finds minimum-cost recovery paths from a source service outward through
 * dependent services. Traverses the reverse graph so that recovery flows
 * from the root dependency to dependents.
 *
 * Edge weight = restartCost of the destination node.
 *
 * Complexity: O(E log V)
 */
@Component
public class DijkstraUtil {

    /**
     * Compute minimum-cost recovery paths from a source service.
     *
     * @param source the starting service (root dependency to restart first)
     * @param graph  the service dependency graph
     * @return source, cost map, and reconstructed paths for every reachable node
     */
    public DijkstraData findMinCostRecovery(String source, Graph graph) {
        Map<String, ServiceNode> nodes = graph.getNodes();
        Map<String, List<Edge>> reverseAdj = graph.getReverseAdjList();

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> visited = new HashSet<>();

        // Initialize all distances to infinity
        for (String nodeId : nodes.keySet()) {
            dist.put(nodeId, Double.MAX_VALUE);
        }
        dist.put(source, nodes.get(source).getRestartCost());

        // Min-heap: entries are (cost, nodeId)
        PriorityQueue<Map.Entry<Double, String>> pq = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getKey)
        );
        pq.offer(Map.entry(dist.get(source), source));

        while (!pq.isEmpty()) {
            Map.Entry<Double, String> top = pq.poll();
            double cost = top.getKey();
            String current = top.getValue();

            if (visited.contains(current)) continue;
            visited.add(current);

            // Traverse reverse edges: from current dependency → dependent services
            for (Edge edge : reverseAdj.getOrDefault(current, List.of())) {
                String dependent = edge.getFrom();
                double newCost = cost + nodes.get(dependent).getRestartCost();

                if (newCost < dist.get(dependent)) {
                    dist.put(dependent, newCost);
                    prev.put(dependent, current);
                    pq.offer(Map.entry(newCost, dependent));
                }
            }
        }

        // Reconstruct paths for all nodes
        Map<String, List<String>> paths = new HashMap<>();
        for (String nodeId : nodes.keySet()) {
            paths.put(nodeId, reconstructPath(source, nodeId, prev));
        }

        return new DijkstraData(source, dist, paths);
    }

    /**
     * Reconstruct the recovery path from source to target using the prev map.
     */
    private List<String> reconstructPath(String source, String target, Map<String, String> prev) {
        LinkedList<String> path = new LinkedList<>();
        String current = target;

        while (current != null) {
            path.addFirst(current);
            if (current.equals(source)) break;
            current = prev.get(current);
        }

        // If path doesn't start at source, no path exists
        if (path.isEmpty() || !path.getFirst().equals(source)) {
            return List.of();
        }
        return path;
    }

    /**
     * Result of Dijkstra computation.
     */
    public record DijkstraData(
            String source,
            Map<String, Double> costs,
            Map<String, List<String>> paths
    ) {}
}
