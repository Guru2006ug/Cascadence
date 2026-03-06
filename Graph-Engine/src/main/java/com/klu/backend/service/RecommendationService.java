package com.klu.backend.service;

import com.klu.backend.algorithm.DFSUtil;
import com.klu.backend.algorithm.DSU;
import com.klu.backend.algorithm.MSTUtil;
import com.klu.backend.dto.response.RecommendationResult;
import com.klu.backend.dto.response.RecommendationResult.Recommendation;
import com.klu.backend.model.Edge;
import com.klu.backend.model.Graph;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tier 3 — Recommendations & Auto-Hardening Suggestions.
 *
 * Composes data from T1 (structural algorithms) and T2 (risk analysis)
 * into a prioritized, actionable list of architecture improvements.
 *
 * Recommendation categories:
 *   1. SPOF_ELIMINATION    (HIGH)   — Nodes whose failure impacts ≥50% of system
 *   2. EDGE_HARDENING      (HIGH)   — Edges with highest failure probability on critical paths
 *   3. REDUNDANCY          (MEDIUM) — Nodes with only a single incoming dependency
 *   4. CLUSTER_ISOLATION   (MEDIUM) — Oversized clusters (>70% of all nodes)
 *   5. COST_OPTIMIZATION   (LOW)    — Non-MST edges that could be removed for savings
 *
 * All analysis is read-only. Graph is never modified.
 */
@Service
public class RecommendationService {

    private final GraphService graphService;
    private final DFSUtil dfsUtil;
    private final MSTUtil mstUtil;

    public RecommendationService(GraphService graphService,
                                  DFSUtil dfsUtil,
                                  MSTUtil mstUtil) {
        this.graphService = graphService;
        this.dfsUtil = dfsUtil;
        this.mstUtil = mstUtil;
    }

    /**
     * Generate a comprehensive, prioritized list of all recommendations.
     *
     * Order: HIGH priority first, then MEDIUM, then LOW.
     * Within each priority level, most impactful recommendations come first.
     */
    public RecommendationResult generateRecommendations() {
        Graph graph = graphService.getGraph();

        if (graph.getNodeCount() == 0) {
            return new RecommendationResult(
                    List.of(new Recommendation("INFO", "LOW", "system",
                            "Graph is empty. Add services and dependencies to receive recommendations.",
                            "N/A")),
                    1,
                    Map.of("LOW", 1),
                    Map.of("INFO", 1)
            );
        }

        List<Recommendation> all = new ArrayList<>();

        // ── 1. SPOF Elimination (HIGH) ──
        all.addAll(generateSpofRecommendations(graph));

        // ── 2. Edge Hardening (HIGH) ──
        all.addAll(generateEdgeHardeningRecommendations(graph));

        // ── 3. Redundancy (MEDIUM) ──
        all.addAll(generateRedundancyRecommendations(graph));

        // ── 4. Cluster Isolation (MEDIUM) ──
        all.addAll(generateClusterRecommendations(graph));

        // ── 5. Cost Optimization (LOW) ──
        all.addAll(generateCostRecommendations(graph));

        // Sort: HIGH → MEDIUM → LOW
        all.sort(Comparator.comparingInt(r -> priorityOrder(r.priority())));

        // Aggregate counts
        Map<String, Integer> byPriority = all.stream()
                .collect(Collectors.groupingBy(Recommendation::priority,
                        Collectors.summingInt(r -> 1)));
        Map<String, Integer> byType = all.stream()
                .collect(Collectors.groupingBy(Recommendation::type,
                        Collectors.summingInt(r -> 1)));

        return new RecommendationResult(all, all.size(), byPriority, byType);
    }

    // ━━━━━━━━━━━━━━━━━━━ 1. SPOF ELIMINATION ━━━━━━━━━━━━━━━━━━━

    /**
     * Identify nodes whose deterministic cascade affects ≥50% of the system.
     * These are single points of failure requiring redundancy.
     */
    private List<Recommendation> generateSpofRecommendations(Graph graph) {
        List<Recommendation> recs = new ArrayList<>();
        int totalNodes = graph.getNodeCount();
        double threshold = totalNodes * 0.5;

        // Compute cascade impact for every node, sorted by impact descending
        List<Map.Entry<String, DFSUtil.CascadeData>> ranked = new ArrayList<>();
        for (String nodeId : graph.getNodes().keySet()) {
            DFSUtil.CascadeData cascade = dfsUtil.simulateCascade(nodeId, graph);
            if (cascade.affectedCount() >= threshold) {
                ranked.add(Map.entry(nodeId, cascade));
            }
        }
        ranked.sort((a, b) -> Integer.compare(
                b.getValue().affectedCount(), a.getValue().affectedCount()));

        for (Map.Entry<String, DFSUtil.CascadeData> entry : ranked) {
            String nodeId = entry.getKey();
            DFSUtil.CascadeData cascade = entry.getValue();

            recs.add(new Recommendation(
                    "SPOF_ELIMINATION",
                    "HIGH",
                    nodeId,
                    String.format("Service '%s' is a single point of failure. "
                                    + "Its failure cascades to %d of %d services (depth %d). "
                                    + "Add alternative dependency routes to reduce its criticality.",
                            nodeId, cascade.affectedCount(), totalNodes, cascade.cascadeDepth()),
                    String.format("Affects %.0f%% of the system",
                            cascade.impactScore() * 100)
            ));
        }
        return recs;
    }

    // ━━━━━━━━━━━━━━━━━━━ 2. EDGE HARDENING ━━━━━━━━━━━━━━━━━━━

    /**
     * Identify high-probability edges on critical dependency paths.
     *
     * An edge is a hardening candidate if:
     *   - failureProbability ≥ 0.6 (high risk of propagation)
     *   - it's on a path from a high-impact source
     *
     * Sorted by probability descending (highest risk first).
     * Limited to top 5 recommendations.
     */
    private List<Recommendation> generateEdgeHardeningRecommendations(Graph graph) {
        List<Recommendation> recs = new ArrayList<>();
        int totalNodes = graph.getNodeCount();

        // Collect all high-probability edges
        List<Edge> candidates = graph.getAllEdges().stream()
                .filter(e -> e.getFailureProbability() >= 0.6)
                .sorted((a, b) -> Double.compare(
                        b.getFailureProbability(), a.getFailureProbability()))
                .limit(5)
                .collect(Collectors.toList());

        for (Edge edge : candidates) {
            // Check downstream impact: if 'to' fails, how many are affected?
            DFSUtil.CascadeData cascade = dfsUtil.simulateCascade(edge.getTo(), graph);

            recs.add(new Recommendation(
                    "EDGE_HARDENING",
                    "HIGH",
                    edge.getFrom() + " → " + edge.getTo(),
                    String.format("Dependency '%s → %s' has high failure probability (%.0f%%). "
                                    + "If '%s' fails, %d services are affected. "
                                    + "Reduce propagation risk by adding circuit breakers or retries.",
                            edge.getFrom(), edge.getTo(),
                            edge.getFailureProbability() * 100,
                            edge.getTo(), cascade.affectedCount()),
                    String.format("Halving probability would reduce propagation risk on a path affecting %d nodes",
                            cascade.affectedCount())
            ));
        }
        return recs;
    }

    // ━━━━━━━━━━━━━━━━━━━ 3. REDUNDANCY ━━━━━━━━━━━━━━━━━━━

    /**
     * Identify nodes with in-degree = 1 that have significant cascade impact.
     *
     * A node with exactly one incoming edge is a redundancy risk:
     * if that single dependency is severed, the node becomes isolated.
     */
    private List<Recommendation> generateRedundancyRecommendations(Graph graph) {
        List<Recommendation> recs = new ArrayList<>();
        Map<String, Integer> inDegree = graph.getInDegree();

        // Find nodes with exactly 1 incoming dependency
        List<String> singleDep = inDegree.entrySet().stream()
                .filter(e -> e.getValue() == 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        for (String nodeId : singleDep) {
            // Find the single dependency source
            String dependsOn = graph.getReverseAdjList()
                    .getOrDefault(nodeId, List.of()).stream()
                    .map(Edge::getFrom)
                    .findFirst()
                    .orElse("unknown");

            DFSUtil.CascadeData cascade = dfsUtil.simulateCascade(nodeId, graph);

            // Only recommend if this node has meaningful cascade impact (>1 affected)
            if (cascade.affectedCount() > 1) {
                recs.add(new Recommendation(
                        "REDUNDANCY",
                        "MEDIUM",
                        nodeId,
                        String.format("Service '%s' has only one dependency path (via '%s'). "
                                        + "If '%s' fails, '%s' becomes unreachable, "
                                        + "affecting %d downstream services. "
                                        + "Add a backup dependency for fault tolerance.",
                                nodeId, dependsOn, dependsOn, nodeId,
                                cascade.affectedCount() - 1),
                        String.format("Single dependency path; %d downstream services at risk",
                                cascade.affectedCount() - 1)
                ));
            }
        }
        return recs;
    }

    // ━━━━━━━━━━━━━━━━━━━ 4. CLUSTER ISOLATION ━━━━━━━━━━━━━━━━━━━

    /**
     * Detect oversized service clusters.
     *
     * If a single connected component contains >70% of all nodes,
     * recommend splitting it into smaller isolated groups.
     */
    private List<Recommendation> generateClusterRecommendations(Graph graph) {
        List<Recommendation> recs = new ArrayList<>();
        int totalNodes = graph.getNodeCount();

        if (totalNodes <= 2) return recs;

        // Build DSU to find clusters
        DSU dsu = new DSU(graph.getNodes().keySet());
        for (Edge edge : graph.getAllEdges()) {
            dsu.union(edge.getFrom(), edge.getTo());
        }

        List<List<String>> clusters = dsu.getClusters();
        double threshold = totalNodes * 0.7;

        for (List<String> cluster : clusters) {
            if (cluster.size() > threshold) {
                recs.add(new Recommendation(
                        "CLUSTER_ISOLATION",
                        "MEDIUM",
                        "cluster[" + String.join(", ", cluster) + "]",
                        String.format("A single cluster contains %d of %d services (%.0f%%). "
                                        + "A failure in this cluster can cascade widely. "
                                        + "Consider splitting into smaller isolated service groups.",
                                cluster.size(), totalNodes,
                                (double) cluster.size() / totalNodes * 100),
                        String.format("Cluster contains %.0f%% of all services",
                                (double) cluster.size() / totalNodes * 100)
                ));
            }
        }
        return recs;
    }

    // ━━━━━━━━━━━━━━━━━━━ 5. COST OPTIMIZATION ━━━━━━━━━━━━━━━━━━━

    /**
     * Compare actual infrastructure cost vs. MST optimal cost.
     *
     * If there's a significant gap (>20% savings possible),
     * recommend reviewing non-essential edges.
     */
    private List<Recommendation> generateCostRecommendations(Graph graph) {
        List<Recommendation> recs = new ArrayList<>();

        if (graph.getEdgeCount() == 0) return recs;

        // Compute actual total infra cost
        double actualCost = graph.getAllEdges().stream()
                .mapToDouble(Edge::getInfraCost)
                .sum();

        // Compute MST cost
        MSTUtil.MSTData mstData = mstUtil.computeMST(graph);
        double mstCost = mstData.totalCost();

        if (actualCost > 0 && mstCost < actualCost) {
            double savings = actualCost - mstCost;
            double savingsPercent = (savings / actualCost) * 100;

            if (savingsPercent > 20) {
                // Identify non-MST edges
                Set<String> mstEdgeKeys = mstData.edges().stream()
                        .map(e -> e.getFrom() + "→" + e.getTo())
                        .collect(Collectors.toSet());

                List<String> removable = graph.getAllEdges().stream()
                        .filter(e -> !mstEdgeKeys.contains(e.getFrom() + "→" + e.getTo())
                                && !mstEdgeKeys.contains(e.getTo() + "→" + e.getFrom()))
                        .map(e -> e.getFrom() + " → " + e.getTo()
                                + " (cost: " + e.getInfraCost() + ")")
                        .collect(Collectors.toList());

                recs.add(new Recommendation(
                        "COST_OPTIMIZATION",
                        "LOW",
                        "infrastructure",
                        String.format("Current total infrastructure cost: %.1f. "
                                        + "Minimum spanning tree cost: %.1f. "
                                        + "Potential savings of %.1f (%.0f%%) by reviewing non-essential edges: %s",
                                actualCost, mstCost, savings, savingsPercent,
                                String.join("; ", removable)),
                        String.format("Potential %.0f%% infrastructure cost reduction", savingsPercent)
                ));
            }
        }
        return recs;
    }

    // ━━━━━━━━━━━━━━━━━━━ HELPERS ━━━━━━━━━━━━━━━━━━━

    /** Map priority to sort order: HIGH=0, MEDIUM=1, LOW=2 */
    private int priorityOrder(String priority) {
        return switch (priority) {
            case "HIGH"   -> 0;
            case "MEDIUM" -> 1;
            case "LOW"    -> 2;
            default       -> 3;
        };
    }
}
