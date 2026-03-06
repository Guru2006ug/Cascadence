package com.klu.backend.controller;

import com.klu.backend.dto.request.AddDependencyRequest;
import com.klu.backend.dto.request.AddServiceRequest;
import com.klu.backend.dto.request.GraphLoadRequest;
import com.klu.backend.dto.response.*;
import com.klu.backend.service.GraphService;
import com.klu.backend.service.OptimizationService;
import com.klu.backend.service.RecoveryService;
import com.klu.backend.service.SimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API controller for the Graph-Based Resilience Engine.
 *
 * Endpoint groups:
 *   /api/graph/**          — Graph management (CRUD)
 *   /api/simulation/**     — Failure simulation
 *   /api/recovery/**       — Recovery planning
 *   /api/optimization/**   — Cost & infrastructure optimization
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GraphController {

    private final GraphService graphService;
    private final SimulationService simulationService;
    private final RecoveryService recoveryService;
    private final OptimizationService optimizationService;

    public GraphController(GraphService graphService,
                        SimulationService simulationService,
                        RecoveryService recoveryService,
                        OptimizationService optimizationService) {
        this.graphService = graphService;
        this.simulationService = simulationService;
        this.recoveryService = recoveryService;
        this.optimizationService = optimizationService;
    }

    // ══════════════════════════ GRAPH MANAGEMENT ══════════════════════════

    /** Get full graph state (all services and dependencies). */
    @GetMapping("/graph")
    public ResponseEntity<GraphStateResponse> getGraph() {
        return ResponseEntity.ok(graphService.getGraphState());
    }

    /** Add a new service node. */
    @PostMapping("/graph/services")
    public ResponseEntity<Map<String, String>> addService(@RequestBody AddServiceRequest request) {
        graphService.addService(request);
        return ResponseEntity.ok(Map.of("message", "Service added: " + request.id()));
    }

    /** Remove a service node and all its edges. */
    @DeleteMapping("/graph/services/{id}")
    public ResponseEntity<Map<String, String>> removeService(@PathVariable String id) {
        graphService.removeService(id);
        return ResponseEntity.ok(Map.of("message", "Service removed: " + id));
    }

    /** Add a dependency edge between two services. */
    @PostMapping("/graph/dependencies")
    public ResponseEntity<Map<String, String>> addDependency(@RequestBody AddDependencyRequest request) {
        graphService.addDependency(request);
        return ResponseEntity.ok(Map.of(
                "message", "Dependency added: " + request.from() + " → " + request.to()));
    }

    /** Remove a dependency edge. */
    @DeleteMapping("/graph/dependencies")
    public ResponseEntity<Map<String, String>> removeDependency(
            @RequestParam String from, @RequestParam String to) {
        graphService.removeDependency(from, to);
        return ResponseEntity.ok(Map.of(
                "message", "Dependency removed: " + from + " → " + to));
    }

    /** Update the restart cost of a service. */
    @PutMapping("/graph/services/{id}/cost")
    public ResponseEntity<Map<String, String>> updateCost(
            @PathVariable String id, @RequestParam double cost) {
        graphService.updateRestartCost(id, cost);
        return ResponseEntity.ok(Map.of("message", "Restart cost updated for: " + id));
    }

    /** Bulk-load an entire graph from JSON. Clears existing graph. */
    @PostMapping("/graph/load")
    public ResponseEntity<Map<String, String>> loadGraph(@RequestBody GraphLoadRequest request) {
        graphService.loadGraph(request);
        return ResponseEntity.ok(Map.of("message", "Graph loaded with "
                + request.services().size() + " services and "
                + request.dependencies().size() + " dependencies"));
    }

    /** Clear the entire graph. */
    @DeleteMapping("/graph")
    public ResponseEntity<Map<String, String>> clearGraph() {
        graphService.clearGraph();
        return ResponseEntity.ok(Map.of("message", "Graph cleared"));
    }

    // ══════════════════════════ SIMULATION ══════════════════════════

    /** Simulate cascading failure from a given service. */
    @PostMapping("/simulation/cascade")
    public ResponseEntity<CascadeResult> simulateCascade(@RequestParam String failedNode) {
        return ResponseEntity.ok(simulationService.simulateCascade(failedNode));
    }

    // ══════════════════════════ RECOVERY ══════════════════════════

    /** Get safe recovery plan (restart order + cycle check + critical chain). */
    @GetMapping("/recovery/plan")
    public ResponseEntity<RecoveryResult> getRecoveryPlan() {
        return ResponseEntity.ok(recoveryService.computeRecoveryPlan());
    }

    /** Quick cycle check — returns true if dependency graph has a cycle. */
    @GetMapping("/recovery/cycle-check")
    public ResponseEntity<Map<String, Boolean>> checkCycle() {
        return ResponseEntity.ok(Map.of("hasCycle", recoveryService.checkCycle()));
    }

    // ══════════════════════════ OPTIMIZATION ══════════════════════════

    /** Find minimum-cost recovery paths from a source service (Dijkstra). */
    @GetMapping("/optimization/dijkstra")
    public ResponseEntity<DijkstraResult> dijkstra(@RequestParam String source) {
        return ResponseEntity.ok(optimizationService.findMinCostRecovery(source));
    }

    /** Detect independent service clusters (DSU). */
    @GetMapping("/optimization/clusters")
    public ResponseEntity<ClusterResult> getClusters() {
        return ResponseEntity.ok(optimizationService.detectClusters());
    }

    /** Compute minimum spanning tree for infrastructure optimization (Kruskal). */
    @GetMapping("/optimization/mst")
    public ResponseEntity<MSTResult> getMST() {
        return ResponseEntity.ok(optimizationService.computeMST());
    }
}
