package com.klu.backend.dto.response;

import java.util.List;
import java.util.Map;

/**
 * Tier 3 — Prioritized architecture improvement recommendations.
 *
 * Composes insights from T1 (structural) and T2 (risk) layers
 * into actionable, prioritized suggestions.
 *
 * Types:
 *   EDGE_HARDENING      — Reduce failure probability on critical edges
 *   REDUNDANCY          — Add backup dependency paths
 *   SPOF_ELIMINATION    — Mitigate single points of failure
 *   CLUSTER_ISOLATION   — Break oversized clusters into smaller groups
 *   COST_OPTIMIZATION   — Remove non-essential edges to reduce infra cost
 *
 * @param recommendations     ordered list (highest priority first)
 * @param totalRecommendations count
 * @param countByPriority     HIGH/MEDIUM/LOW → count
 * @param countByType         recommendation type → count
 */
public record RecommendationResult(
        List<Recommendation> recommendations,
        int totalRecommendations,
        Map<String, Integer> countByPriority,
        Map<String, Integer> countByType
) {
    /**
     * A single actionable recommendation.
     *
     * @param type            category (EDGE_HARDENING, REDUNDANCY, etc.)
     * @param priority        HIGH / MEDIUM / LOW
     * @param target          the affected node or edge (e.g., "auth → user-svc")
     * @param description     human-readable explanation
     * @param expectedImpact  quantified impact description
     */
    public record Recommendation(
            String type,
            String priority,
            String target,
            String description,
            String expectedImpact
    ) {}
}
