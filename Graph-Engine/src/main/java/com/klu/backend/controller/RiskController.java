package com.klu.backend.controller;

import com.klu.backend.dto.request.WhatIfRequest;
import com.klu.backend.dto.response.*;
import com.klu.backend.service.RiskAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Tier 2 — Risk & Intelligence REST API.
 *
 * Endpoints:
 *   POST /api/risk/monte-carlo       — Probabilistic cascade simulation
 *   GET  /api/risk/critical-nodes     — Rank nodes by failure impact
 *   GET  /api/risk/resilience         — System fragility & resilience score
 *   GET  /api/risk/sensitivity        — Rank edges by hardening impact
 *   POST /api/risk/what-if            — Compare current vs. modified architecture
 */
@RestController
@RequestMapping("/api/risk")
public class RiskController {

    private final RiskAnalysisService riskAnalysisService;

    public RiskController(RiskAnalysisService riskAnalysisService) {
        this.riskAnalysisService = riskAnalysisService;
    }

    // ─────────── 1. Monte Carlo Simulation ───────────

    @PostMapping("/monte-carlo")
    public ResponseEntity<MonteCarloResult> monteCarlo(
            @RequestParam String failedNode,
            @RequestParam(defaultValue = "1000") int iterations) {
        MonteCarloResult result = riskAnalysisService.runMonteCarlo(failedNode, iterations);
        return ResponseEntity.ok(result);
    }

    // ─────────── 2. Critical Node Detection ───────────

    @GetMapping("/critical-nodes")
    public ResponseEntity<CriticalNodeResult> criticalNodes() {
        CriticalNodeResult result = riskAnalysisService.detectCriticalNodes();
        return ResponseEntity.ok(result);
    }

    // ─────────── 3. Resilience Report ───────────

    @GetMapping("/resilience")
    public ResponseEntity<ResilienceReport> resilience(
            @RequestParam(defaultValue = "1000") int iterations) {
        ResilienceReport result = riskAnalysisService.computeResilience(iterations);
        return ResponseEntity.ok(result);
    }

    // ─────────── 4. Sensitivity Analysis ───────────

    @GetMapping("/sensitivity")
    public ResponseEntity<SensitivityResult> sensitivity(
            @RequestParam(defaultValue = "500") int iterations) {
        SensitivityResult result = riskAnalysisService.analyzeSensitivity(iterations);
        return ResponseEntity.ok(result);
    }

    // ─────────── 5. What-If Engine ───────────

    @PostMapping("/what-if")
    public ResponseEntity<WhatIfResult> whatIf(@RequestBody WhatIfRequest request) {
        WhatIfResult result = riskAnalysisService.runWhatIf(request);
        return ResponseEntity.ok(result);
    }
}
