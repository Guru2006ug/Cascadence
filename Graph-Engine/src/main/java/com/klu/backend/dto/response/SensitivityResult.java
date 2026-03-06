package com.klu.backend.dto.response;

import java.util.List;

/**
 * Response for sensitivity analysis.
 * For each edge, measures how much the system's fragility index changes
 * when that edge's probability is reduced (hardened).
 *
 * @param rankings        edges sorted by risk impact (most sensitive first)
 * @param mostSensitive   the single most impactful dependency
 * @param baselineFragility the current fragilityIndex before any hardening
 */
public record SensitivityResult(
        List<EdgeSensitivity> rankings,
        EdgeSensitivity mostSensitive,
        double baselineFragility
) {
    /**
     * Sensitivity data for a single edge.
     *
     * @param from              dependent service
     * @param to                dependency service
     * @param originalProbability   current failure probability
     * @param hardenedProbability   probability after hardening (halved)
     * @param fragilityBefore   system fragility with original probability
     * @param fragilityAfter    system fragility with hardened probability
     * @param riskReduction     fragilityBefore - fragilityAfter (higher = more impactful)
     */
    public record EdgeSensitivity(
            String from,
            String to,
            double originalProbability,
            double hardenedProbability,
            double fragilityBefore,
            double fragilityAfter,
            double riskReduction
    ) {}
}
