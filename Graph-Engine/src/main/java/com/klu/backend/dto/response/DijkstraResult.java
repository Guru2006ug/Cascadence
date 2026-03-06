package com.klu.backend.dto.response;

import java.util.List;
import java.util.Map;

/**
 * Response for Dijkstra cost-optimized recovery.
 *
 * @param source            the starting service
 * @param recoveryCosts     minimum cumulative recovery cost to reach each service (-1 if unreachable)
 * @param recoveryPaths     optimal recovery path from source to each service
 * @param totalRecoveryCost sum of all reachable recovery costs
 */
public record DijkstraResult(
        String source,
        Map<String, Double> recoveryCosts,
        Map<String, List<String>> recoveryPaths,
        double totalRecoveryCost
) {}
