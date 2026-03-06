package com.klu.backend.dto.response;

import java.util.Map;

/**
 * Response for what-if comparison analysis.
 * Compares the system's risk profile before and after a user-specified modification.
 *
 * @param beforeFragility      fragilityIndex before modification
 * @param afterFragility       fragilityIndex after modification
 * @param beforeResilience     resilienceScore before modification
 * @param afterResilience      resilienceScore after modification
 * @param fragilityDelta       change in fragility (negative = improvement)
 * @param resilienceDelta      change in resilience (positive = improvement)
 * @param beforePerNode        per-node failure probability before
 * @param afterPerNode         per-node failure probability after
 * @param simulationIterations Monte Carlo iterations used
 * @param verdict              human-readable assessment string
 */
public record WhatIfResult(
        double beforeFragility,
        double afterFragility,
        double beforeResilience,
        double afterResilience,
        double fragilityDelta,
        double resilienceDelta,
        Map<String, Double> beforePerNode,
        Map<String, Double> afterPerNode,
        int simulationIterations,
        String verdict
) {}
