package com.klu.backend.dto.response;

import java.util.List;
import java.util.Map;

/**
 * Response for cascading failure simulation.
 *
 * @param failedNode          the initially failed service
 * @param affectedNodes       all services affected (including the initial failure)
 * @param depthMap            mapping of each affected node to its cascade depth level
 * @param cascadeDepth        maximum cascade depth
 * @param impactScore         fraction of total services affected (0.0 to 1.0)
 * @param weightedImpactScore sum of importanceWeights of affected / total weight
 * @param affectedCount       number of affected services
 * @param totalNodes          total number of services in the graph
 */
public record CascadeResult(
        String failedNode,
        List<String> affectedNodes,
        Map<String, Integer> depthMap,
        int cascadeDepth,
        double impactScore,
        double weightedImpactScore,
        int affectedCount,
        int totalNodes
) {}
