package com.klu.backend.dto.response;

import java.util.List;

/**
 * Tier 3 — Side-by-side architecture comparison report.
 *
 * Compares the current live architecture against a proposed design,
 * showing score deltas and categorized improvements/degradations.
 *
 * @param currentScore         full score breakdown for the live graph
 * @param proposedScore        full score breakdown for the proposed graph
 * @param compositeScoreDelta  proposed - current (positive = improvement)
 * @param verdict              human-readable assessment
 * @param improvements         sub-metrics that improved
 * @param degradations         sub-metrics that degraded
 */
public record ArchitectureComparisonResult(
        ArchitectureScoreResult currentScore,
        ArchitectureScoreResult proposedScore,
        double compositeScoreDelta,
        String verdict,
        List<String> improvements,
        List<String> degradations
) {}
