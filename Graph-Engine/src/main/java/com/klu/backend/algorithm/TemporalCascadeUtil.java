package com.klu.backend.algorithm;

import com.klu.backend.model.Edge;
import com.klu.backend.model.Graph;
import com.klu.backend.model.ServiceNode;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Time-based cascading failure simulation using a priority queue.
 *
 * Unlike DFSUtil (instant BFS), this engine models real-world propagation delays:
 * each edge has a propagationDelay (seconds) that determines WHEN the failure
 * reaches the dependent service.
 *
 * Uses a min-heap (PriorityQueue) ordered by failure time.
 * At each edge, failure propagates only if random ≤ failureProbability
 * (same probabilistic gating as MonteCarloUtil).
 *
 * Produces a chronological timeline of failure events + aggregate metrics:
 *   - CascadeDuration       : time span from first to last failure
 *   - TimeToSystemCollapse  : time until ≥50% of services have failed (-1 if never)
 *   - PeakFailureMoment     : timestamp with the most concurrent failures
 *
 * Complexity: O((V + E) log V) due to priority queue operations.
 */
@Component
public class TemporalCascadeUtil {

    /**
     * Simulate time-based cascade from a failed node.
     *
     * @param failedNode the initially failed service
     * @param graph      the service dependency graph
     * @return temporal cascade data with timeline and metrics
     */
    public TemporalCascadeData simulate(String failedNode, Graph graph) {
        Map<String, List<Edge>> reverseAdj = graph.getReverseAdjList();
        Map<String, ServiceNode> nodes = graph.getNodes();
        int totalNodes = nodes.size();

        // Min-heap ordered by failure time
        PriorityQueue<Event> events = new PriorityQueue<>(
                Comparator.comparingDouble(Event::time));

        Set<String> failed = new LinkedHashSet<>();
        List<TimelineEntry> timeline = new ArrayList<>();
        Map<Double, Integer> failuresAtTime = new TreeMap<>();

        // Seed: initial failure at t=0
        events.add(new Event(failedNode, 0.0, 0, null));

        double collapseThreshold = totalNodes * 0.5;
        double timeToCollapse = -1.0;

        while (!events.isEmpty()) {
            Event event = events.poll();

            if (failed.contains(event.nodeId())) {
                continue; // already failed
            }

            failed.add(event.nodeId());
            timeline.add(new TimelineEntry(
                    event.nodeId(), event.time(), event.level(), event.triggeredBy()));

            // Track failures at this timestamp
            failuresAtTime.merge(event.time(), 1, Integer::sum);

            // Check if system collapse threshold reached
            if (timeToCollapse < 0 && failed.size() >= collapseThreshold) {
                timeToCollapse = event.time();
            }

            // Schedule dependent failures
            for (Edge edge : reverseAdj.getOrDefault(event.nodeId(), List.of())) {
                String dependent = edge.getFrom();
                if (!failed.contains(dependent)) {
                    // Probabilistic gate
                    double roll = ThreadLocalRandom.current().nextDouble();
                    if (roll <= edge.getFailureProbability()) {
                        double newTime = event.time() + edge.getPropagationDelay();
                        events.add(new Event(dependent, newTime, event.level() + 1, event.nodeId()));
                    }
                }
            }
        }

        // Compute metrics
        double cascadeDuration = timeline.isEmpty() ? 0.0
                : timeline.get(timeline.size() - 1).failureTime() - timeline.get(0).failureTime();

        // Peak failure moment: timestamp with most concurrent failures
        double peakMoment = 0.0;
        int peakCount = 0;
        for (Map.Entry<Double, Integer> entry : failuresAtTime.entrySet()) {
            if (entry.getValue() > peakCount) {
                peakCount = entry.getValue();
                peakMoment = entry.getKey();
            }
        }

        // Weighted impact
        double totalWeight = nodes.values().stream()
                .mapToDouble(ServiceNode::getImportanceWeight).sum();
        double affectedWeight = failed.stream()
                .mapToDouble(id -> nodes.get(id).getImportanceWeight()).sum();
        double weightedImpact = totalWeight > 0 ? affectedWeight / totalWeight : 0.0;

        double impactScore = totalNodes > 0 ? (double) failed.size() / totalNodes : 0.0;

        return new TemporalCascadeData(
                failedNode,
                timeline,
                round(cascadeDuration),
                round(timeToCollapse),
                round(peakMoment),
                peakCount,
                failed.size(),
                totalNodes,
                round(impactScore),
                round(weightedImpact)
        );
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    // ──────────── Internal Records ────────────

    private record Event(String nodeId, double time, int level, String triggeredBy) {}

    public record TimelineEntry(String nodeId, double failureTime, int cascadeLevel,
                                 String triggeredBy) {}

    public record TemporalCascadeData(
            String failedNode,
            List<TimelineEntry> timeline,
            double cascadeDuration,
            double timeToSystemCollapse,
            double peakFailureMoment,
            int peakConcurrentFailures,
            int totalAffected,
            int totalNodes,
            double impactScore,
            double weightedImpactScore
    ) {}
}
