package com.klu.backend.dto.response;

import java.util.Map;

/**
 * Response for Monte Carlo probabilistic cascade simulation.
 *
 * @param failedNode            the initially failed service
 * @param iterations            number of simulation runs
 * @param failureCounts         how many times each node failed across iterations
 * @param failureProbabilities  failure probability per node (failCount / N)
 * @param expectedCascadeSize   average number of affected nodes per run
 * @param fragilityIndex        expectedCascadeSize / totalNodes (0.0 = robust, 1.0 = fragile)
 * @param totalNodes            total services in the graph
 */
public record MonteCarloResult(
        String failedNode,
        int iterations,
        Map<String, Integer> failureCounts,
        Map<String, Double> failureProbabilities,
        double expectedCascadeSize,
        double fragilityIndex,
        int totalNodes
) {}
