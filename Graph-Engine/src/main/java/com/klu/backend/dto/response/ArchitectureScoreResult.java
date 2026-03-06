package com.klu.backend.dto.response;

import java.util.List;
import java.util.Map;

/**
 * Tier 3 — Composite Architecture Health Score.
 *
 * Combines five sub-metrics into a single 0-100 score with a letter grade.
 *
 * Weights:
 *   Resilience  30%  — How well the system absorbs cascading failures
 *   Redundancy  20%  — Whether services have backup dependency paths
 *   Coupling    20%  — Edge density (lower coupling = better)
 *   Depth       15%  — Longest dependency chain (shallower = better)
 *   SPOF        15%  — Single points of failure (fewer = better)
 *
 * Grade:  A (80-100)  B (65-79)  C (50-64)  D (35-49)  F (0-34)
 *
 * @param compositeScore     weighted 0-100 score
 * @param grade              letter grade (A/B/C/D/F)
 * @param resilienceScore    0.0-1.0 from Monte Carlo analysis
 * @param redundancyScore    0.0-1.0 ratio of nodes with backup paths
 * @param couplingScore      0.0-1.0 inverted edge density (1 = loosely coupled)
 * @param depthScore         0.0-1.0 inverse of chain depth (1 = shallow)
 * @param spofScore          0.0-1.0 inverse of SPOF ratio (1 = no SPOFs)
 * @param spofCount          number of single points of failure
 * @param subScores          named map of all sub-scores
 * @param recommendations    actionable improvement suggestions
 * @param totalNodes         service count
 * @param totalEdges         dependency count
 */
public record ArchitectureScoreResult(
        double compositeScore,
        String grade,
        double resilienceScore,
        double redundancyScore,
        double couplingScore,
        double depthScore,
        double spofScore,
        int spofCount,
        Map<String, Double> subScores,
        List<String> recommendations,
        int totalNodes,
        int totalEdges
) {}
