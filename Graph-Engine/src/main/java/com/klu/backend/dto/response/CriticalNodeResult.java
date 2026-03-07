package com.klu.backend.dto.response;

import java.util.List;

/**
 * Response for critical node detection.
 * Each node is ranked by its deterministic cascade impact.
 *
 * @param rankings      nodes sorted by impact (most critical first)
 * @param mostCritical  the single most critical node ID
 * @param totalNodes    total services in the graph
 */
public record CriticalNodeResult(
        List<NodeImpact> rankings,
        String mostCritical,
        int totalNodes
) {
    /**
     * Impact data for a single node.
     *
     * @param nodeId              service identifier
     * @param affectedCount       number of services affected if this node fails
     * @param impactScore         affectedCount / totalNodes
     * @param weightedImpactScore sum of importanceWeights of affected / total weight
     * @param cascadeDepth        max cascade depth from this node
     */
    public record NodeImpact(
            String nodeId,
            int affectedCount,
            double impactScore,
            double weightedImpactScore,
            int cascadeDepth
    ) {}
}
