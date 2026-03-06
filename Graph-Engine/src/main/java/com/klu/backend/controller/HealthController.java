package com.klu.backend.controller;

import com.klu.backend.dto.request.NodeStateRequest;
import com.klu.backend.dto.response.HealthDashboard;
import com.klu.backend.dto.response.LiveRecoveryPlan;
import com.klu.backend.model.ServiceState;
import com.klu.backend.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Tier 3 — Health & Real-Time State Management REST API.
 *
 * Endpoints:
 *   PUT  /api/health/node/{id}/state  — Change node health state
 *   GET  /api/health/dashboard        — Live health overview
 *   POST /api/health/recovery-plan    — Optimal recovery plan for failed nodes
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    // ─────────── 1. Node State Transition ───────────

    /**
     * Update the health state of a service node.
     *
     * Valid states: HEALTHY, FAILED, RECOVERING (case-insensitive).
     *
     * @param id      the service identifier
     * @param request body containing { "state": "FAILED" }
     * @return confirmation with old and new state
     */
    @PutMapping("/node/{id}/state")
    public ResponseEntity<Map<String, String>> updateState(
            @PathVariable String id,
            @RequestBody NodeStateRequest request) {
        ServiceState newState = healthService.updateNodeState(id, request.state());
        return ResponseEntity.ok(Map.of(
                "message", "State updated for service: " + id,
                "nodeId", id,
                "newState", newState.name()
        ));
    }

    // ─────────── 2. Health Dashboard ───────────

    /**
     * Get a live health overview of the entire system.
     *
     * Includes node state counts, lists of failed/recovering services,
     * cascade risk zones from failed nodes, and estimated recovery cost.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<HealthDashboard> getDashboard() {
        HealthDashboard dashboard = healthService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }

    // ─────────── 3. Recovery Plan ───────────

    /**
     * Compute optimal recovery plan for currently failed nodes.
     *
     * Returns step-by-step restart instructions with correct dependency
     * ordering, individual and cumulative costs, and prerequisites.
     */
    @PostMapping("/recovery-plan")
    public ResponseEntity<LiveRecoveryPlan> getRecoveryPlan() {
        LiveRecoveryPlan plan = healthService.computeRecoveryPlan();
        return ResponseEntity.ok(plan);
    }
}
