package com.klu.backend.dto.response;

import java.util.List;
import java.util.Map;

/**
 * Tier 3 — Live system health dashboard.
 *
 * Aggregates real-time node state information and identifies
 * active cascade risk zones emanating from currently failed nodes.
 *
 * @param totalNodes             total services in the graph
 * @param healthyCount           nodes in HEALTHY state
 * @param failedCount            nodes in FAILED state
 * @param recoveringCount        nodes in RECOVERING state
 * @param healthyNodes           IDs of healthy services
 * @param failedNodes            IDs of failed services
 * @param recoveringNodes        IDs of recovering services
 * @param cascadeRiskZones       failedNode → list of healthy nodes at risk from that failure
 * @param estimatedRecoveryCost  sum of restartCost for all failed nodes
 * @param systemHealthPercentage percentage of nodes in HEALTHY state
 */
public record HealthDashboard(
        int totalNodes,
        int healthyCount,
        int failedCount,
        int recoveringCount,
        List<String> healthyNodes,
        List<String> failedNodes,
        List<String> recoveringNodes,
        Map<String, List<String>> cascadeRiskZones,
        double estimatedRecoveryCost,
        double systemHealthPercentage
) {}
