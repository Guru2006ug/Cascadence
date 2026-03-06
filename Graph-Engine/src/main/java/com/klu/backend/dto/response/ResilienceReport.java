package com.klu.backend.dto.response;

import java.util.Map;

/**
 * Response for overall architecture resilience assessment.
 *
 * FragilityIndex    = average expected cascade size / total nodes
 *                     (averaged across all possible single-node failures)
 * ResilienceScore   = 1.0 - FragilityIndex
 *
 * @param fragilityIndex         how fragile the architecture is (0.0 = robust, 1.0 = fragile)
 * @param resilienceScore        how resilient (1.0 = robust, 0.0 = fragile)
 * @param averageCascadeSize     mean cascade size across all failure origins
 * @param perNodeFragility       fragility index when each specific node fails
 * @param monteCarloIterations   iterations used per node for probabilistic computation
 * @param totalNodes             total services in the graph
 */
public record ResilienceReport(
        double fragilityIndex,
        double resilienceScore,
        double averageCascadeSize,
        Map<String, Double> perNodeFragility,
        int monteCarloIterations,
        int totalNodes
) {}
