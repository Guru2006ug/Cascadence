package com.klu.backend.dto.response;

import java.util.List;

/**
 * Response for temporal (time-based) cascade simulation.
 *
 * Models failure propagation over time using priority-queue scheduling,
 * producing a chronological timeline of failure events.
 *
 * @param failedNode             the initially failed service
 * @param timeline               chronological list of failure events
 * @param cascadeDuration        total time from first to last failure (seconds)
 * @param timeToSystemCollapse   time until ≥50% of services have failed (seconds, -1 if never reached)
 * @param peakFailureMoment      timestamp with the most concurrent failures (seconds)
 * @param peakConcurrentFailures max number of services failing at the same timestamp
 * @param totalAffected          total number of services that failed
 * @param totalNodes             total services in the graph
 * @param impactScore            totalAffected / totalNodes
 * @param weightedImpactScore    sum of importanceWeights of affected nodes / total weight
 */
public record TemporalCascadeResult(
        String failedNode,
        List<TemporalEvent> timeline,
        double cascadeDuration,
        double timeToSystemCollapse,
        double peakFailureMoment,
        int peakConcurrentFailures,
        int totalAffected,
        int totalNodes,
        double impactScore,
        double weightedImpactScore
) {
    /**
     * A single failure event in the temporal cascade.
     *
     * @param nodeId       the service that failed
     * @param failureTime  time of failure in seconds since initial event
     * @param cascadeLevel depth level in the cascade chain
     * @param triggeredBy  which service caused this failure (null for initial)
     */
    public record TemporalEvent(
            String nodeId,
            double failureTime,
            int cascadeLevel,
            String triggeredBy
    ) {}
}
