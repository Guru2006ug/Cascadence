package com.klu.backend.service;

import com.klu.backend.algorithm.DFSUtil;
import com.klu.backend.algorithm.MonteCarloUtil;
import com.klu.backend.algorithm.TopoSortUtil;
import com.klu.backend.dto.request.GraphLoadRequest;
import com.klu.backend.dto.response.ArchitectureComparisonResult;
import com.klu.backend.dto.response.ArchitectureScoreResult;
import com.klu.backend.model.Edge;
import com.klu.backend.model.Graph;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Tier 3 — Architecture Scoring & Comparison Engine.
 *
 * Computes a composite 0-100 architecture health score by combining:
 *   1. Resilience   (30%) — Monte Carlo system-wide fragility
 *   2. Redundancy   (20%) — Ratio of nodes with backup dependency paths
 *   3. Coupling     (20%) — Inverted edge density (lower coupling = better)
 *   4. Depth        (15%) — Inverse of longest chain length
 *   5. SPOF         (15%) — Inverse of single-point-of-failure ratio
 *
 * Grade mapping:  A (80-100)  B (65-79)  C (50-64)  D (35-49)  F (0-34)
 *
 * All scoring methods accept a Graph parameter so they work on both
 * the live graph and temporary graphs (for comparison).
 *
 * Does NOT modify any graph. Pure read-only analysis.
 */
@Service
public class ArchitectureService {

    private static final int SCORE_ITERATIONS = 500;

    private static final double W_RESILIENCE  = 0.30;
    private static final double W_REDUNDANCY  = 0.20;
    private static final double W_COUPLING    = 0.20;
    private static final double W_DEPTH       = 0.15;
    private static final double W_SPOF        = 0.15;

    private final GraphService graphService;
    private final MonteCarloUtil monteCarloUtil;
    private final DFSUtil dfsUtil;
    private final TopoSortUtil topoSortUtil;

    public ArchitectureService(GraphService graphService,
                                MonteCarloUtil monteCarloUtil,
                                DFSUtil dfsUtil,
                                TopoSortUtil topoSortUtil) {
        this.graphService = graphService;
        this.monteCarloUtil = monteCarloUtil;
        this.dfsUtil = dfsUtil;
        this.topoSortUtil = topoSortUtil;
    }

    // ━━━━━━━━━━━━━━━━━━━ 1. ARCHITECTURE SCORE ━━━━━━━━━━━━━━━━━━━

    /**
     * Compute the architecture health score for the current live graph.
     */
    public ArchitectureScoreResult computeScore() {
        return computeScoreForGraph(graphService.getGraph());
    }

    /**
     * Core scoring engine — works on any Graph instance.
     *
     * Computes five sub-metrics, each normalized to [0.0, 1.0],
     * then combines them with weights into a 0-100 composite score.
     */
    public ArchitectureScoreResult computeScoreForGraph(Graph graph) {
        int totalNodes = graph.getNodeCount();
        int totalEdges = graph.getEdgeCount();

        if (totalNodes == 0) {
            return emptyScore();
        }

        // ── Sub-metric 1: Resilience (Monte Carlo) ──
        double resilienceScore = computeResilienceScore(graph);

        // ── Sub-metric 2: Redundancy ──
        double redundancyScore = computeRedundancyScore(graph);

        // ── Sub-metric 3: Coupling (inverted edge density) ──
        double couplingScore = computeCouplingScore(graph, totalNodes, totalEdges);

        // ── Sub-metric 4: Depth (inverse of longest chain) ──
        double depthScore = computeDepthScore(graph, totalNodes);

        // ── Sub-metric 5: SPOF (inverse of SPOF ratio) ──
        List<String> spofs = detectSPOFs(graph, totalNodes);
        double spofScore = computeSpofScore(spofs.size(), totalNodes);

        // ── Composite Score ──
        double composite = (resilienceScore * W_RESILIENCE
                          + redundancyScore * W_REDUNDANCY
                          + couplingScore   * W_COUPLING
                          + depthScore      * W_DEPTH
                          + spofScore       * W_SPOF) * 100.0;
        composite = round(composite);

        String grade = computeGrade(composite);

        Map<String, Double> subScores = new LinkedHashMap<>();
        subScores.put("resilience", round(resilienceScore));
        subScores.put("redundancy", round(redundancyScore));
        subScores.put("coupling", round(couplingScore));
        subScores.put("depth", round(depthScore));
        subScores.put("spof", round(spofScore));

        List<String> recommendations = generateRecommendations(
                resilienceScore, redundancyScore, couplingScore,
                depthScore, spofs.size(), totalNodes);

        return new ArchitectureScoreResult(
                composite, grade,
                round(resilienceScore), round(redundancyScore),
                round(couplingScore), round(depthScore), round(spofScore),
                spofs.size(), subScores, recommendations,
                totalNodes, totalEdges
        );
    }

    // ━━━━━━━━━━━━━━━━━━━ 2. ARCHITECTURE COMPARISON ━━━━━━━━━━━━━━━━━━━

    /**
     * Compare the current live architecture against a proposed design.
     *
     * Builds a temporary Graph from the request, scores both, and
     * computes deltas. The live graph is never modified.
     *
     * @param proposed the proposed architecture definition
     * @return side-by-side comparison with deltas and verdict
     */
    public ArchitectureComparisonResult compareArchitecture(GraphLoadRequest proposed) {
        ArchitectureScoreResult currentScore = computeScore();
        Graph tempGraph = buildTemporaryGraph(proposed);
        ArchitectureScoreResult proposedScore = computeScoreForGraph(tempGraph);

        double delta = round(proposedScore.compositeScore() - currentScore.compositeScore());

        // Categorize improvements and degradations
        List<String> improvements = new ArrayList<>();
        List<String> degradations = new ArrayList<>();

        compareMetric("Resilience", currentScore.resilienceScore(),
                proposedScore.resilienceScore(), improvements, degradations);
        compareMetric("Redundancy", currentScore.redundancyScore(),
                proposedScore.redundancyScore(), improvements, degradations);
        compareMetric("Coupling", currentScore.couplingScore(),
                proposedScore.couplingScore(), improvements, degradations);
        compareMetric("Depth", currentScore.depthScore(),
                proposedScore.depthScore(), improvements, degradations);
        compareMetric("SPOF", currentScore.spofScore(),
                proposedScore.spofScore(), improvements, degradations);

        String verdict = buildComparisonVerdict(delta);

        return new ArchitectureComparisonResult(
                currentScore, proposedScore, delta,
                verdict, improvements, degradations
        );
    }

    // ━━━━━━━━━━━━━━━━━━━ SUB-METRIC COMPUTATIONS ━━━━━━━━━━━━━━━━━━━

    /**
     * Resilience: 1 - average fragility across all single-node failures.
     * Uses Monte Carlo with reduced iterations for performance.
     */
    private double computeResilienceScore(Graph graph) {
        int totalNodes = graph.getNodeCount();
        double totalFragility = 0.0;

        for (String nodeId : graph.getNodes().keySet()) {
            MonteCarloUtil.MonteCarloData data =
                    monteCarloUtil.simulate(nodeId, graph, SCORE_ITERATIONS);
            totalFragility += data.fragilityIndex();
        }

        double avgFragility = totalFragility / totalNodes;
        return 1.0 - avgFragility;
    }

    /**
     * Redundancy: ratio of non-root nodes that have in-degree ≥ 2.
     *
     * A node with in-degree ≥ 2 has at least two services depending on it
     * through different paths, indicating structural backup.
     * If no non-root nodes exist, redundancy = 1.0 (trivially redundant).
     */
    private double computeRedundancyScore(Graph graph) {
        Map<String, Integer> inDegree = graph.getInDegree();
        long nodesWithDeps = inDegree.values().stream().filter(d -> d >= 1).count();

        if (nodesWithDeps == 0) return 1.0;

        long nodesWithBackup = inDegree.values().stream().filter(d -> d >= 2).count();
        return (double) nodesWithBackup / nodesWithDeps;
    }

    /**
     * Coupling: inverted edge density.
     *
     * Density = E / (V * (V-1)) for a directed graph.
     * Score = 1 - density  (lower density = higher score).
     */
    private double computeCouplingScore(Graph graph, int totalNodes, int totalEdges) {
        if (totalNodes <= 1) return 1.0;

        double maxEdges = (double) totalNodes * (totalNodes - 1);
        double density = totalEdges / maxEdges;
        return Math.max(0.0, 1.0 - density);
    }

    /**
     * Depth: inverse of normalized longest chain.
     *
     * longestPath / (V-1) is the ratio of actual depth to max possible.
     * Score = 1 - (longestPath / (V-1)).
     * Shallower architectures score higher.
     */
    private double computeDepthScore(Graph graph, int totalNodes) {
        if (totalNodes <= 1) return 1.0;

        int longestChain = topoSortUtil.longestPath(graph);
        double maxPossible = totalNodes - 1.0;
        double normalized = longestChain / maxPossible;
        return Math.max(0.0, 1.0 - normalized);
    }

    /**
     * Detect single points of failure.
     *
     * A SPOF is a node whose deterministic cascade affects ≥ 50% of the network.
     */
    private List<String> detectSPOFs(Graph graph, int totalNodes) {
        List<String> spofs = new ArrayList<>();
        double threshold = totalNodes * 0.5;

        for (String nodeId : graph.getNodes().keySet()) {
            DFSUtil.CascadeData cascade = dfsUtil.simulateCascade(nodeId, graph);
            if (cascade.affectedCount() >= threshold) {
                spofs.add(nodeId);
            }
        }
        return spofs;
    }

    /**
     * SPOF Score: 1 - (spofCount / totalNodes).
     * Fewer SPOFs relative to network size = higher score.
     */
    private double computeSpofScore(int spofCount, int totalNodes) {
        if (totalNodes == 0) return 1.0;
        return 1.0 - ((double) spofCount / totalNodes);
    }

    // ━━━━━━━━━━━━━━━━━━━ GRADING & RECOMMENDATIONS ━━━━━━━━━━━━━━━━━━━

    private String computeGrade(double composite) {
        if (composite >= 80) return "A";
        if (composite >= 65) return "B";
        if (composite >= 50) return "C";
        if (composite >= 35) return "D";
        return "F";
    }

    private List<String> generateRecommendations(
            double resilience, double redundancy, double coupling,
            double depth, int spofCount, int totalNodes) {

        List<String> recs = new ArrayList<>();

        if (resilience < 0.5) {
            recs.add("CRITICAL: System resilience is below 50%. "
                    + "Harden high-probability failure edges immediately.");
        } else if (resilience < 0.7) {
            recs.add("WARNING: Resilience is moderate (" + pct(resilience) + "%). "
                    + "Consider reducing failure probability on critical dependencies.");
        }

        if (redundancy < 0.3) {
            recs.add("CRITICAL: Most services lack backup dependency paths. "
                    + "Add redundant connections to improve fault tolerance.");
        } else if (redundancy < 0.6) {
            recs.add("WARNING: Only " + pct(redundancy) + "% of dependent services "
                    + "have backup paths. Add alternative routes.");
        }

        if (coupling < 0.6) {
            recs.add("WARNING: System is highly coupled (density > 40%). "
                    + "Consider breaking into smaller isolated service clusters.");
        }

        if (depth < 0.5) {
            recs.add("WARNING: Deep dependency chains detected. "
                    + "Flatten the architecture to reduce cascade propagation depth.");
        }

        if (spofCount > 0) {
            recs.add("HIGH: " + spofCount + " single point(s) of failure detected "
                    + "(each affects ≥50% of " + totalNodes + " services). "
                    + "Add alternative dependency routes to eliminate SPOFs.");
        }

        if (recs.isEmpty()) {
            recs.add("Architecture is healthy. No immediate action required.");
        }

        return recs;
    }

    // ━━━━━━━━━━━━━━━━━━━ HELPERS ━━━━━━━━━━━━━━━━━━━

    /**
     * Build a temporary Graph from a GraphLoadRequest (for comparison).
     * Does not affect the live graph in any way.
     */
    private Graph buildTemporaryGraph(GraphLoadRequest request) {
        Graph temp = new Graph();
        for (GraphLoadRequest.ServiceInput svc : request.services()) {
            temp.addNode(svc.id(), svc.restartCost());
        }
        for (GraphLoadRequest.DependencyInput dep : request.dependencies()) {
            temp.addEdge(dep.from(), dep.to(), dep.failureProbability(), dep.infraCost());
        }
        return temp;
    }

    private void compareMetric(String name, double current, double proposed,
                                List<String> improvements, List<String> degradations) {
        double delta = proposed - current;
        if (delta > 0.01) {
            improvements.add(String.format("%s improved by %.1f%% (%.2f → %.2f)",
                    name, delta * 100, current, proposed));
        } else if (delta < -0.01) {
            degradations.add(String.format("%s degraded by %.1f%% (%.2f → %.2f)",
                    name, Math.abs(delta) * 100, current, proposed));
        }
    }

    private String buildComparisonVerdict(double delta) {
        if (delta > 1.0) {
            return String.format("Proposed architecture IMPROVES overall score by %.1f points", delta);
        } else if (delta < -1.0) {
            return String.format("Proposed architecture DEGRADES overall score by %.1f points",
                    Math.abs(delta));
        } else {
            return "Proposed architecture has NEGLIGIBLE impact on overall score";
        }
    }

    private ArchitectureScoreResult emptyScore() {
        return new ArchitectureScoreResult(
                100.0, "A", 1.0, 1.0, 1.0, 1.0, 1.0, 0,
                Map.of("resilience", 1.0, "redundancy", 1.0, "coupling", 1.0,
                        "depth", 1.0, "spof", 1.0),
                List.of("Empty graph — add services and dependencies to evaluate."),
                0, 0
        );
    }

    private String pct(double d) {
        return String.format("%.0f", d * 100);
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
