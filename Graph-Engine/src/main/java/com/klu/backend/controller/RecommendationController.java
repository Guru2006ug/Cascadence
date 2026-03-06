package com.klu.backend.controller;

import com.klu.backend.dto.response.RecommendationResult;
import com.klu.backend.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tier 3 — Recommendations & Auto-Hardening REST API.
 *
 * Endpoint:
 *   GET /api/recommendations — Prioritized architecture improvement suggestions
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Generate a prioritized list of architecture improvement recommendations.
     *
     * Analyzes the current graph for:
     *   - Single Points of Failure (HIGH)
     *   - Edge Hardening opportunities (HIGH)
     *   - Redundancy gaps (MEDIUM)
     *   - Oversized clusters (MEDIUM)
     *   - Infrastructure cost savings (LOW)
     */
    @GetMapping
    public ResponseEntity<RecommendationResult> getRecommendations() {
        RecommendationResult result = recommendationService.generateRecommendations();
        return ResponseEntity.ok(result);
    }
}
