package com.klu.backend.controller;

import com.klu.backend.dto.request.GraphLoadRequest;
import com.klu.backend.dto.response.ArchitectureComparisonResult;
import com.klu.backend.dto.response.ArchitectureScoreResult;
import com.klu.backend.service.ArchitectureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Tier 3 — Architecture Scoring & Comparison REST API.
 *
 * Endpoints:
 *   GET  /api/architecture/score    — Composite architecture health score (0-100)
 *   POST /api/architecture/compare  — Score current vs. proposed architecture
 */
@RestController
@RequestMapping("/api/architecture")
public class ArchitectureController {

    private final ArchitectureService architectureService;

    public ArchitectureController(ArchitectureService architectureService) {
        this.architectureService = architectureService;
    }

    // ─────────── 1. Architecture Score ───────────

    /**
     * Compute composite architecture health score for the current live graph.
     *
     * Returns a 0-100 score with letter grade, sub-metric breakdown,
     * SPOF count, and actionable recommendations.
     */
    @GetMapping("/score")
    public ResponseEntity<ArchitectureScoreResult> getScore() {
        ArchitectureScoreResult result = architectureService.computeScore();
        return ResponseEntity.ok(result);
    }

    // ─────────── 2. Architecture Comparison ───────────

    /**
     * Compare the current architecture against a proposed design.
     *
     * Request body uses the same format as /api/graph/load:
     * { "services": [...], "dependencies": [...] }
     *
     * The proposed graph is built temporarily — the live graph is never modified.
     */
    @PostMapping("/compare")
    public ResponseEntity<ArchitectureComparisonResult> compare(
            @RequestBody GraphLoadRequest proposedArchitecture) {
        ArchitectureComparisonResult result =
                architectureService.compareArchitecture(proposedArchitecture);
        return ResponseEntity.ok(result);
    }
}
