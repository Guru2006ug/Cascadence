package com.klu.backend.algorithm;

import com.klu.backend.model.CorrelationGroup;
import com.klu.backend.model.Edge;
import com.klu.backend.model.Graph;
import com.klu.backend.model.ServiceNode;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monte Carlo probabilistic failure propagation engine.
 *
 * Upgrades over basic version:
 *   1. PARALLEL execution — splits iterations across threads via ExecutorService
 *   2. CONFIDENCE INTERVALS — 95% CI for each node's failure probability
 *   3. WEIGHTED IMPACT — uses ServiceNode.importanceWeight for realistic scoring
 *   4. CORRELATED FAILURES — when a node fails, correlation group members co-fail
 *
 * Uses BFS on the reverse graph with probabilistic gating.
 *
 * Complexity: O(N × (V + E) / T) where T = thread count
 */
@Component
public class MonteCarloUtil {

    private static final double Z_95 = 1.96;  // z-score for 95% confidence interval
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * Run Monte Carlo probabilistic cascade simulation in parallel.
     *
     * @param failedNode  the initially failed service
     * @param graph       the service dependency graph
     * @param iterations  number of simulation runs (e.g., 1000)
     * @return per-node failure counts, probabilities, CIs, weighted impact, and aggregate metrics
     */
    public MonteCarloData simulate(String failedNode, Graph graph, int iterations) {
        Map<String, List<Edge>> reverseAdj = graph.getReverseAdjList();
        Map<String, ServiceNode> nodes = graph.getNodes();
        Set<String> allNodeIds = nodes.keySet();
        int totalNodes = allNodeIds.size();
        Map<String, CorrelationGroup> corrGroups = graph.getCorrelationGroups();

        // Build node → groups index for fast lookup
        Map<String, List<CorrelationGroup>> nodeToGroups = buildNodeGroupIndex(corrGroups, allNodeIds);

        // ── PARALLEL EXECUTION ──
        Map<String, Integer> failureCounts = new ConcurrentHashMap<>();
        for (String node : allNodeIds) {
            failureCounts.put(node, 0);
        }
        AtomicLong totalAffected = new AtomicLong(0);
        AtomicLong totalWeightedImpactX10000 = new AtomicLong(0);

        double totalWeight = nodes.values().stream()
                .mapToDouble(ServiceNode::getImportanceWeight).sum();

        ExecutorService pool = Executors.newFixedThreadPool(
                Math.min(THREAD_COUNT, Math.max(1, iterations)));
        try {
            int batchSize = Math.max(1, iterations / THREAD_COUNT);
            List<Future<?>> futures = new ArrayList<>();

            for (int t = 0; t < iterations; t += batchSize) {
                int batchStart = t;
                int batchEnd = Math.min(t + batchSize, iterations);

                futures.add(pool.submit(() -> {
                    for (int i = batchStart; i < batchEnd; i++) {
                        Set<String> failed = runSingleSimulation(
                                failedNode, reverseAdj, allNodeIds, nodeToGroups);

                        totalAffected.addAndGet(failed.size());

                        // Weighted impact for this run
                        double runWeight = 0.0;
                        for (String node : failed) {
                            failureCounts.merge(node, 1, Integer::sum);
                            runWeight += nodes.get(node).getImportanceWeight();
                        }
                        double normalizedWeight = totalWeight > 0 ? runWeight / totalWeight : 0.0;
                        totalWeightedImpactX10000.addAndGet((long) (normalizedWeight * 10000));
                    }
                }));
            }

            // Wait for all batches
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Monte Carlo simulation interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Monte Carlo simulation failed", e);
        } finally {
            pool.shutdown();
        }

        // ── COMPUTE RESULTS ──
        Map<String, Double> failureProbabilities = new LinkedHashMap<>();
        Map<String, double[]> confidenceIntervals = new LinkedHashMap<>();

        for (String nodeId : allNodeIds) {
            double p = (double) failureCounts.get(nodeId) / iterations;
            failureProbabilities.put(nodeId, p);

            // 95% Confidence Interval: p ± z * sqrt(p(1-p)/n)
            double margin = Z_95 * Math.sqrt(p * (1.0 - p) / iterations);
            double lower = Math.max(0.0, p - margin);
            double upper = Math.min(1.0, p + margin);
            confidenceIntervals.put(nodeId, new double[]{
                    round(lower), round(p), round(upper)
            });
        }

        double expectedCascadeSize = (double) totalAffected.get() / iterations;
        double fragilityIndex = totalNodes > 0
                ? expectedCascadeSize / totalNodes
                : 0.0;
        double weightedExpectedImpact = (double) totalWeightedImpactX10000.get() / (iterations * 10000.0);

        return new MonteCarloData(
                failedNode,
                iterations,
                new LinkedHashMap<>(failureCounts),
                failureProbabilities,
                confidenceIntervals,
                expectedCascadeSize,
                weightedExpectedImpact,
                fragilityIndex
        );
    }

    /**
     * Run a single probabilistic cascade iteration.
     * BFS on reverse graph: at each edge, fail only if random ≤ probability.
     * Also triggers correlated failures when a node in a group fails.
     */
    private Set<String> runSingleSimulation(String failedNode,
                                             Map<String, List<Edge>> reverseAdj,
                                             Set<String> allNodes,
                                             Map<String, List<CorrelationGroup>> nodeToGroups) {
        Set<String> failed = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        failed.add(failedNode);
        queue.add(failedNode);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            // ── Correlated failures: check if this node is in any group ──
            List<CorrelationGroup> groups = nodeToGroups.getOrDefault(current, List.of());
            for (CorrelationGroup group : groups) {
                for (String member : group.getNodeIds()) {
                    if (!failed.contains(member)) {
                        double roll = ThreadLocalRandom.current().nextDouble();
                        if (roll <= group.getCorrelationFactor()) {
                            failed.add(member);
                            queue.add(member);
                        }
                    }
                }
            }

            // ── Standard edge-based propagation ──
            for (Edge edge : reverseAdj.getOrDefault(current, List.of())) {
                String dependent = edge.getFrom();
                if (!failed.contains(dependent)) {
                    double roll = ThreadLocalRandom.current().nextDouble();
                    if (roll <= edge.getFailureProbability()) {
                        failed.add(dependent);
                        queue.add(dependent);
                    }
                }
            }
        }

        return failed;
    }

    /**
     * Build an index of nodeId → list of CorrelationGroups that contain it.
     */
    private Map<String, List<CorrelationGroup>> buildNodeGroupIndex(
            Map<String, CorrelationGroup> corrGroups, Set<String> allNodeIds) {
        Map<String, List<CorrelationGroup>> index = new HashMap<>();
        for (CorrelationGroup group : corrGroups.values()) {
            for (String nodeId : group.getNodeIds()) {
                if (allNodeIds.contains(nodeId)) {
                    index.computeIfAbsent(nodeId, k -> new ArrayList<>()).add(group);
                }
            }
        }
        return index;
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    /**
     * Result of Monte Carlo simulation.
     */
    public record MonteCarloData(
            String failedNode,
            int iterations,
            Map<String, Integer> failureCounts,
            Map<String, Double> failureProbabilities,
            Map<String, double[]> confidenceIntervals,
            double expectedCascadeSize,
            double weightedExpectedImpact,
            double fragilityIndex
    ) {}
}
