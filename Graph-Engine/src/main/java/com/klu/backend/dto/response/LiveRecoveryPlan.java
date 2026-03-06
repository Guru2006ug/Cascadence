package com.klu.backend.dto.response;

import java.util.List;

/**
 * Tier 3 — Step-by-step recovery plan for currently failed nodes.
 *
 * Uses topological sort to determine correct restart order
 * (dependencies first) and aggregates cost estimates.
 *
 * @param steps                  ordered recovery steps
 * @param failedNodes            IDs of all currently failed services
 * @param totalEstimatedCost     sum of restartCost for all failed nodes
 * @param recoveryDepth          number of sequential recovery steps
 * @param hasCyclicDependencies  true if graph contains a cycle (recovery order unreliable)
 */
public record LiveRecoveryPlan(
        List<RecoveryStep> steps,
        List<String> failedNodes,
        double totalEstimatedCost,
        int recoveryDepth,
        boolean hasCyclicDependencies
) {
    /**
     * A single step in the recovery sequence.
     *
     * @param order             1-based step number
     * @param nodeId            service to restart
     * @param restartCost       cost to restart this service
     * @param cumulativeCost    running total cost up to (and including) this step
     * @param prerequisiteNodes failed dependencies that must be restarted first
     */
    public record RecoveryStep(
            int order,
            String nodeId,
            double restartCost,
            double cumulativeCost,
            List<String> prerequisiteNodes
    ) {}
}
