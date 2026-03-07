package com.klu.backend.service;

import com.klu.backend.algorithm.DFSUtil;
import com.klu.backend.algorithm.MonteCarloUtil;
import com.klu.backend.dto.request.WhatIfRequest;
import com.klu.backend.dto.response.*;
import com.klu.backend.exception.GraphValidationException;
import com.klu.backend.model.Edge;
import com.klu.backend.model.Graph;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tier 2 — Risk & Intelligence Layer.
 *
 * Orchestrates:
 *   1. Monte Carlo probabilistic simulation
 *   2. Critical node detection
 *   3. Resilience scoring (fragility index)
 *   4. Sensitivity analysis (edge ranking)
 *   5. What-if comparison engine
 *
 * Does NOT modify the main graph. All analysis is read-only.
 * What-if uses temporary graph clones.
 */
@Service
public class RiskAnalysisService {

    private static final int DEFAULT_ITERATIONS = 1000;

    private final GraphService graphService;
    private final MonteCarloUtil monteCarloUtil;
    private final DFSUtil dfsUtil;

    public RiskAnalysisService(GraphService graphService,
                                MonteCarloUtil monteCarloUtil,
                                DFSUtil dfsUtil) {
        this.graphService = graphService;
        this.monteCarloUtil = monteCarloUtil;
        this.dfsUtil = dfsUtil;
    }

    // ━━━━━━━━━━━━━━━━━━━ 1. MONTE CARLO SIMULATION ━━━━━━━━━━━━━━━━━━━

    /**
     * Run Monte Carlo probabilistic cascade simulation from a given node.
     *
     * @param failedNode  the service that initially fails
     * @param iterations  number of simulation runs (default 1000)
     * @return per-node failure probabilities and aggregate metrics
     */
    public MonteCarloResult runMonteCarlo(String failedNode, int iterations) {
        Graph graph = graphService.getGraph();
        validateNode(graph, failedNode);

        int runs = iterations > 0 ? iterations : DEFAULT_ITERATIONS;
        MonteCarloUtil.MonteCarloData data = monteCarloUtil.simulate(failedNode, graph, runs);

        return new MonteCarloResult(
                data.failedNode(),
                data.iterations(),
                data.failureCounts(),
                data.failureProbabilities(),
                data.confidenceIntervals(),
                data.expectedCascadeSize(),
                data.weightedExpectedImpact(),
                data.fragilityIndex(),
                graph.getNodeCount()
        );
    }

    // ━━━━━━━━━━━━━━━━━━━ 2. CRITICAL NODE DETECTION ━━━━━━━━━━━━━━━━━━━

    /**
     * For every node in the graph, run deterministic cascade and rank by impact.
     * The most critical node is the one whose failure affects the most services.
     *
     * @return ranked list of nodes by cascade impact
     */
    public CriticalNodeResult detectCriticalNodes() {
        Graph graph = graphService.getGraph();

        if (graph.getNodeCount() == 0) {
            return new CriticalNodeResult(List.of(), "", 0);
        }

        List<CriticalNodeResult.NodeImpact> rankings = new ArrayList<>();

        for (String nodeId : graph.getNodes().keySet()) {
            DFSUtil.CascadeData cascade = dfsUtil.simulateCascade(nodeId, graph);
            rankings.add(new CriticalNodeResult.NodeImpact(
                    nodeId,
                    cascade.affectedCount(),
                    cascade.impactScore(),
                    cascade.weightedImpactScore(),
                    cascade.cascadeDepth()
            ));
        }

        // Sort by affectedCount descending, then by cascadeDepth descending
        rankings.sort((a, b) -> {
            int cmp = Integer.compare(b.affectedCount(), a.affectedCount());
            return cmp != 0 ? cmp : Integer.compare(b.cascadeDepth(), a.cascadeDepth());
        });

        String mostCritical = rankings.get(0).nodeId();

        return new CriticalNodeResult(rankings, mostCritical, graph.getNodeCount());
    }

    // ━━━━━━━━━━━━━━━━━━━ 3. RESILIENCE SCORING ━━━━━━━━━━━━━━━━━━━

    /**
     * Compute overall architecture resilience using Monte Carlo.
     *
     * For each node, simulate it failing → get fragilityIndex.
     * Average all per-node fragility indices → system-wide fragilityIndex.
     * ResilienceScore = 1.0 - fragilityIndex.
     *
     * @param iterations Monte Carlo iterations per node
     * @return resilience report with fragility index and per-node breakdown
     */
    public ResilienceReport computeResilience(int iterations) {
        Graph graph = graphService.getGraph();
        int totalNodes = graph.getNodeCount();

        if (totalNodes == 0) {
            return new ResilienceReport(0.0, 1.0, 0.0, Map.of(), iterations, 0);
        }

        int runs = iterations > 0 ? iterations : DEFAULT_ITERATIONS;
        Map<String, Double> perNodeFragility = new LinkedHashMap<>();
        double totalFragility = 0.0;
        double totalCascade = 0.0;

        for (String nodeId : graph.getNodes().keySet()) {
            MonteCarloUtil.MonteCarloData data = monteCarloUtil.simulate(nodeId, graph, runs);
            perNodeFragility.put(nodeId, round(data.fragilityIndex()));
            totalFragility += data.fragilityIndex();
            totalCascade += data.expectedCascadeSize();
        }

        double fragilityIndex = totalFragility / totalNodes;
        double resilienceScore = 1.0 - fragilityIndex;
        double avgCascadeSize = totalCascade / totalNodes;

        return new ResilienceReport(
                round(fragilityIndex),
                round(resilienceScore),
                round(avgCascadeSize),
                perNodeFragility,
                runs,
                totalNodes
        );
    }

    // ━━━━━━━━━━━━━━━━━━━ 4. SENSITIVITY ANALYSIS ━━━━━━━━━━━━━━━━━━━

    /**
     * For each edge, temporarily halve its probability and measure how much
     * the system-wide fragility index changes.
     *
     * The edge whose hardening causes the largest fragility drop is the
     * most sensitive (highest-priority for hardening).
     *
     * @param iterations Monte Carlo iterations per evaluation
     * @return ranked edge sensitivity list
     */
    public SensitivityResult analyzeSensitivity(int iterations) {
        Graph graph = graphService.getGraph();
        int runs = iterations > 0 ? iterations : DEFAULT_ITERATIONS;

        // Compute baseline fragility
        double baselineFragility = computeSystemFragility(graph, runs);

        List<Edge> allEdges = graph.getAllEdges();
        List<SensitivityResult.EdgeSensitivity> rankings = new ArrayList<>();

        for (Edge edge : allEdges) {
            double originalProb = edge.getFailureProbability();
            double hardenedProb = originalProb / 2.0;

            // Temporarily modify edge probability
            edge.setFailureProbability(hardenedProb);

            double hardenedFragility = computeSystemFragility(graph, runs);

            // Restore original probability
            edge.setFailureProbability(originalProb);

            double riskReduction = baselineFragility - hardenedFragility;

            rankings.add(new SensitivityResult.EdgeSensitivity(
                    edge.getFrom(),
                    edge.getTo(),
                    round(originalProb),
                    round(hardenedProb),
                    round(baselineFragility),
                    round(hardenedFragility),
                    round(riskReduction)
            ));
        }

        // Sort by riskReduction descending (most impactful first)
        rankings.sort((a, b) -> Double.compare(b.riskReduction(), a.riskReduction()));

        SensitivityResult.EdgeSensitivity mostSensitive = rankings.isEmpty()
                ? null : rankings.get(0);

        return new SensitivityResult(rankings, mostSensitive, round(baselineFragility));
    }

    // ━━━━━━━━━━━━━━━━━━━ 5. WHAT-IF ENGINE ━━━━━━━━━━━━━━━━━━━

    /**
     * Compare current architecture risk vs. modified architecture.
     *
     * Process:
     *   1. Compute "before" metrics on current graph
     *   2. Apply modifications temporarily
     *   3. Compute "after" metrics
     *   4. Restore original graph state
     *   5. Return comparison delta
     *
     * @param request what-if modification specification
     * @return before/after comparison with deltas and verdict
     */
    public WhatIfResult runWhatIf(WhatIfRequest request) {
        Graph graph = graphService.getGraph();
        String failedNode = request.failedNode();
        int runs = request.iterations() > 0 ? request.iterations() : DEFAULT_ITERATIONS;

        validateNode(graph, failedNode);

        // ── BEFORE: compute metrics on current graph ──
        MonteCarloUtil.MonteCarloData beforeData = monteCarloUtil.simulate(failedNode, graph, runs);
        double beforeFragility = beforeData.fragilityIndex();
        double beforeResilience = 1.0 - beforeFragility;
        Map<String, Double> beforePerNode = beforeData.failureProbabilities();

        // ── APPLY MODIFICATIONS ──
        // Save originals for rollback
        List<double[]> savedProbabilities = new ArrayList<>();
        List<Edge> addedEdges = new ArrayList<>();
        List<Edge> removedEdges = new ArrayList<>();

        try {
            // Apply edge probability updates
            if (request.edgeUpdates() != null) {
                for (WhatIfRequest.EdgeUpdate update : request.edgeUpdates()) {
                    Edge edge = findEdge(graph, update.from(), update.to());
                    savedProbabilities.add(new double[]{edge.getFailureProbability()});
                    edge.setFailureProbability(update.newProbability());
                }
            }

            // Apply edge additions
            if (request.edgeAdditions() != null) {
                for (WhatIfRequest.EdgeAddition addition : request.edgeAdditions()) {
                    graph.addEdge(addition.from(), addition.to(),
                            addition.probability(), addition.infraCost());
                    addedEdges.add(new Edge(addition.from(), addition.to(),
                            addition.probability(), addition.infraCost()));
                }
            }

            // Apply edge removals
            if (request.edgeRemovals() != null) {
                for (WhatIfRequest.EdgeRemoval removal : request.edgeRemovals()) {
                    Edge removed = findEdge(graph, removal.from(), removal.to());
                    removedEdges.add(new Edge(removed.getFrom(), removed.getTo(),
                            removed.getFailureProbability(), removed.getInfraCost()));
                    graph.removeEdge(removal.from(), removal.to());
                }
            }

            // ── AFTER: compute metrics on modified graph ──
            MonteCarloUtil.MonteCarloData afterData = monteCarloUtil.simulate(failedNode, graph, runs);
            double afterFragility = afterData.fragilityIndex();
            double afterResilience = 1.0 - afterFragility;
            Map<String, Double> afterPerNode = afterData.failureProbabilities();

            double fragilityDelta = afterFragility - beforeFragility;
            double resilienceDelta = afterResilience - beforeResilience;

            String verdict = buildVerdict(fragilityDelta, resilienceDelta);

            return new WhatIfResult(
                    round(beforeFragility),
                    round(afterFragility),
                    round(beforeResilience),
                    round(afterResilience),
                    round(fragilityDelta),
                    round(resilienceDelta),
                    beforePerNode,
                    afterPerNode,
                    runs,
                    verdict
            );
        } finally {
            // ── ROLLBACK: restore original graph state ──
            rollbackModifications(graph, request, savedProbabilities, addedEdges, removedEdges);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━ HELPER METHODS ━━━━━━━━━━━━━━━━━━━

    /**
     * Compute system-wide fragility index by averaging Monte Carlo
     * fragility across all single-node failure scenarios.
     */
    private double computeSystemFragility(Graph graph, int iterations) {
        int totalNodes = graph.getNodeCount();
        if (totalNodes == 0) return 0.0;

        double totalFragility = 0.0;
        for (String nodeId : graph.getNodes().keySet()) {
            MonteCarloUtil.MonteCarloData data = monteCarloUtil.simulate(nodeId, graph, iterations);
            totalFragility += data.fragilityIndex();
        }
        return totalFragility / totalNodes;
    }

    /**
     * Find a specific edge in the graph. Throws if not found.
     */
    private Edge findEdge(Graph graph, String from, String to) {
        return graph.getAdjacencyList()
                .getOrDefault(from, List.of()).stream()
                .filter(e -> e.getTo().equals(to))
                .findFirst()
                .orElseThrow(() -> new GraphValidationException(
                        "Dependency not found: " + from + " → " + to));
    }

    /**
     * Rollback all what-if modifications to restore original graph state.
     */
    private void rollbackModifications(Graph graph,
                                        WhatIfRequest request,
                                        List<double[]> savedProbabilities,
                                        List<Edge> addedEdges,
                                        List<Edge> removedEdges) {
        // Restore edge probabilities
        if (request.edgeUpdates() != null) {
            for (int i = 0; i < request.edgeUpdates().size() && i < savedProbabilities.size(); i++) {
                WhatIfRequest.EdgeUpdate update = request.edgeUpdates().get(i);
                try {
                    Edge edge = findEdge(graph, update.from(), update.to());
                    edge.setFailureProbability(savedProbabilities.get(i)[0]);
                } catch (Exception ignored) {
                    // Edge may have been removed in a later step
                }
            }
        }

        // Remove added edges
        for (Edge added : addedEdges) {
            try {
                graph.removeEdge(added.getFrom(), added.getTo());
            } catch (Exception ignored) {
            }
        }

        // Re-add removed edges
        for (Edge removed : removedEdges) {
            try {
                graph.addEdge(removed.getFrom(), removed.getTo(),
                        removed.getFailureProbability(), removed.getInfraCost());
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Build a human-readable verdict string.
     */
    private String buildVerdict(double fragilityDelta, double resilienceDelta) {
        double pct = Math.abs(resilienceDelta) * 100;
        if (resilienceDelta > 0.001) {
            return String.format("Modification IMPROVES resilience by %.1f%%", pct);
        } else if (resilienceDelta < -0.001) {
            return String.format("Modification DEGRADES resilience by %.1f%%", pct);
        } else {
            return "Modification has NEGLIGIBLE impact on resilience";
        }
    }

    private void validateNode(Graph graph, String nodeId) {
        if (!graph.containsNode(nodeId)) {
            throw new GraphValidationException("Service not found: " + nodeId);
        }
    }

    /** Round to 4 decimal places for clean JSON output. */
    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
