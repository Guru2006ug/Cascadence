package com.klu.backend.algorithm;

import com.klu.backend.model.Edge;
import com.klu.backend.model.Graph;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Monte Carlo probabilistic failure propagation engine.
 *
 * Unlike DFSUtil (deterministic — all reachable nodes fail), this engine
 * respects edge failureProbability: at each edge, failure propagates only
 * if a random roll ≤ probability.
 *
 * Run N iterations, track failure counts per node, compute:
 *   - FailureRisk(node) = failCount / N
 *   - ExpectedCascadeSize = avg affected nodes across all runs
 *   - FragilityIndex = ExpectedCascadeSize / TotalNodes
 *
 * Uses BFS on the reverse graph with probabilistic gating.
 *
 * Complexity: O(N × (V + E))
 */
@Component
public class MonteCarloUtil {

    /**
     * Run Monte Carlo probabilistic cascade simulation.
     *
     * @param failedNode  the initially failed service
     * @param graph       the service dependency graph
     * @param iterations  number of simulation runs (e.g., 1000)
     * @return per-node failure counts, probabilities, and aggregate metrics
     */
    public MonteCarloData simulate(String failedNode, Graph graph, int iterations) {
        Map<String, List<Edge>> reverseAdj = graph.getReverseAdjList();
        Set<String> allNodes = graph.getNodes().keySet();
        int totalNodes = allNodes.size();

        // Track how many times each node fails across all iterations
        Map<String, Integer> failureCounts = new LinkedHashMap<>();
        for (String node : allNodes) {
            failureCounts.put(node, 0);
        }

        long totalAffected = 0;

        for (int i = 0; i < iterations; i++) {
            Set<String> failed = runSingleSimulation(failedNode, reverseAdj, allNodes);
            totalAffected += failed.size();

            for (String node : failed) {
                failureCounts.merge(node, 1, Integer::sum);
            }
        }

        // Compute per-node failure probability
        Map<String, Double> failureProbabilities = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : failureCounts.entrySet()) {
            failureProbabilities.put(entry.getKey(),
                    (double) entry.getValue() / iterations);
        }

        double expectedCascadeSize = (double) totalAffected / iterations;
        double fragilityIndex = totalNodes > 0
                ? expectedCascadeSize / totalNodes
                : 0.0;

        return new MonteCarloData(
                failedNode,
                iterations,
                failureCounts,
                failureProbabilities,
                expectedCascadeSize,
                fragilityIndex
        );
    }

    /**
     * Run a single probabilistic cascade iteration.
     * BFS on reverse graph: at each edge, fail only if random ≤ probability.
     */
    private Set<String> runSingleSimulation(String failedNode,
                                             Map<String, List<Edge>> reverseAdj,
                                             Set<String> allNodes) {
        Set<String> failed = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        failed.add(failedNode);
        queue.add(failedNode);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            for (Edge edge : reverseAdj.getOrDefault(current, List.of())) {
                String dependent = edge.getFrom();
                if (!failed.contains(dependent)) {
                    // Probabilistic gate: propagate only if random ≤ failureProbability
                    double roll = ThreadLocalRandom.current().nextDouble();
                    if (roll <= edge.getFailureProbability()) {
                        failed.add(dependent);
                        queue.add(dependent);
                    }
                }
            }
        }

        return failed;
    }

    /**
     * Result of Monte Carlo simulation.
     */
    public record MonteCarloData(
            String failedNode,
            int iterations,
            Map<String, Integer> failureCounts,
            Map<String, Double> failureProbabilities,
            double expectedCascadeSize,
            double fragilityIndex
    ) {}
}
