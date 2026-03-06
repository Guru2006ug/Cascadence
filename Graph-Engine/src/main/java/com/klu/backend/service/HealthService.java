package com.klu.backend.service;

import com.klu.backend.algorithm.DFSUtil;
import com.klu.backend.algorithm.TopoSortUtil;
import com.klu.backend.dto.response.HealthDashboard;
import com.klu.backend.dto.response.LiveRecoveryPlan;
import com.klu.backend.exception.GraphValidationException;
import com.klu.backend.model.Edge;
import com.klu.backend.model.Graph;
import com.klu.backend.model.ServiceNode;
import com.klu.backend.model.ServiceState;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tier 3 — Health & Real-Time State Management.
 *
 * Capabilities:
 *   1. Node state transitions  (HEALTHY ↔ FAILED ↔ RECOVERING)
 *   2. Live health dashboard   (aggregate stats + cascade risk zones)
 *   3. Recovery planning       (optimal restart order for failed nodes)
 *
 * State transitions update the ServiceNode.state field directly.
 * The dashboard and recovery plan are computed dynamically from
 * current node states — no history tracking, fully stateless reads.
 */
@Service
public class HealthService {

    private final GraphService graphService;
    private final DFSUtil dfsUtil;
    private final TopoSortUtil topoSortUtil;

    public HealthService(GraphService graphService,
                          DFSUtil dfsUtil,
                          TopoSortUtil topoSortUtil) {
        this.graphService = graphService;
        this.dfsUtil = dfsUtil;
        this.topoSortUtil = topoSortUtil;
    }

    // ━━━━━━━━━━━━━━━━━━━ 1. NODE STATE MANAGEMENT ━━━━━━━━━━━━━━━━━━━

    /**
     * Update the health state of a service node.
     *
     * Valid transitions: HEALTHY → FAILED, FAILED → RECOVERING, RECOVERING → HEALTHY
     * (all transitions are allowed for flexibility — operator override).
     *
     * @param nodeId the service to update
     * @param state  the target state string (HEALTHY, FAILED, RECOVERING)
     * @return the new state after update
     */
    public ServiceState updateNodeState(String nodeId, String state) {
        Graph graph = graphService.getGraph();

        if (!graph.containsNode(nodeId)) {
            throw new GraphValidationException("Service not found: " + nodeId);
        }

        ServiceState newState;
        try {
            newState = ServiceState.valueOf(state.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new GraphValidationException(
                    "Invalid state: '" + state + "'. Valid: HEALTHY, FAILED, RECOVERING");
        }

        // Graph.getNodes() returns unmodifiable map but the contained
        // ServiceNode objects are the same live references (Lombok @Data).
        // Setting state here directly modifies the node in the graph.
        ServiceNode node = graph.getNodes().get(nodeId);
        node.setState(newState);

        return newState;
    }

    // ━━━━━━━━━━━━━━━━━━━ 2. HEALTH DASHBOARD ━━━━━━━━━━━━━━━━━━━

    /**
     * Compute a live health dashboard from current node states.
     *
     * For each FAILED node, computes the cascade risk zone:
     * which currently HEALTHY nodes would be impacted if the failure
     * propagates deterministically through the dependency graph.
     */
    public HealthDashboard getDashboard() {
        Graph graph = graphService.getGraph();
        Map<String, ServiceNode> nodes = graph.getNodes();
        int totalNodes = nodes.size();

        if (totalNodes == 0) {
            return new HealthDashboard(0, 0, 0, 0,
                    List.of(), List.of(), List.of(),
                    Map.of(), 0.0, 100.0);
        }

        // Partition nodes by state
        List<String> healthy = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        List<String> recovering = new ArrayList<>();

        for (ServiceNode node : nodes.values()) {
            switch (node.getState()) {
                case HEALTHY    -> healthy.add(node.getId());
                case FAILED     -> failed.add(node.getId());
                case RECOVERING -> recovering.add(node.getId());
            }
        }

        // Compute cascade risk zones for each failed node
        // Only include currently HEALTHY nodes in the risk zone
        Set<String> healthySet = new HashSet<>(healthy);
        Map<String, List<String>> cascadeRiskZones = new LinkedHashMap<>();

        for (String failedNode : failed) {
            DFSUtil.CascadeData cascade = dfsUtil.simulateCascade(failedNode, graph);
            List<String> atRisk = cascade.affectedNodes().stream()
                    .filter(n -> !n.equals(failedNode))
                    .filter(healthySet::contains)
                    .collect(Collectors.toList());
            cascadeRiskZones.put(failedNode, atRisk);
        }

        // Estimated recovery cost = sum of restartCost for all failed nodes
        double recoveryCost = failed.stream()
                .mapToDouble(id -> nodes.get(id).getRestartCost())
                .sum();

        double healthPercentage = totalNodes > 0
                ? round((double) healthy.size() / totalNodes * 100.0)
                : 100.0;

        return new HealthDashboard(
                totalNodes,
                healthy.size(),
                failed.size(),
                recovering.size(),
                healthy, failed, recovering,
                cascadeRiskZones,
                recoveryCost,
                healthPercentage
        );
    }

    // ━━━━━━━━━━━━━━━━━━━ 3. RECOVERY PLAN ━━━━━━━━━━━━━━━━━━━

    /**
     * Compute an optimal step-by-step recovery plan for currently failed nodes.
     *
     * Process:
     *   1. Identify all FAILED nodes
     *   2. Run topological sort on the full graph
     *   3. Reverse the topo order (dependencies-first restart)
     *   4. Filter to only include failed nodes
     *   5. Build ordered recovery steps with costs and prerequisites
     *
     * Prerequisites for each step = failed dependencies that must be
     * restarted before this node (from the forward adjacency list).
     */
    public LiveRecoveryPlan computeRecoveryPlan() {
        Graph graph = graphService.getGraph();
        Map<String, ServiceNode> nodes = graph.getNodes();

        // Find all FAILED nodes
        List<String> failedNodes = nodes.values().stream()
                .filter(n -> n.getState() == ServiceState.FAILED)
                .map(ServiceNode::getId)
                .collect(Collectors.toList());

        if (failedNodes.isEmpty()) {
            return new LiveRecoveryPlan(
                    List.of(), List.of(), 0.0, 0, false);
        }

        // Check for cycles
        TopoSortUtil.TopologicalSortData topoData = topoSortUtil.topologicalSort(graph);

        if (topoData.hasCycle()) {
            double totalCost = failedNodes.stream()
                    .mapToDouble(id -> nodes.get(id).getRestartCost())
                    .sum();
            return new LiveRecoveryPlan(
                    List.of(), failedNodes, totalCost, 0, true);
        }

        // Reverse topo order → restart dependencies first
        List<String> restartOrder = new ArrayList<>(topoData.order());
        Collections.reverse(restartOrder);

        // Filter to only failed nodes, maintaining topo-safe order
        Set<String> failedSet = new HashSet<>(failedNodes);
        List<String> filteredOrder = restartOrder.stream()
                .filter(failedSet::contains)
                .collect(Collectors.toList());

        // Build recovery steps
        List<LiveRecoveryPlan.RecoveryStep> steps = new ArrayList<>();
        double cumulativeCost = 0.0;

        for (int i = 0; i < filteredOrder.size(); i++) {
            String nodeId = filteredOrder.get(i);
            ServiceNode node = nodes.get(nodeId);
            cumulativeCost += node.getRestartCost();

            // Prerequisites: failed dependencies this node depends on
            // (from forward adjacency: nodeId → depends on → targets)
            List<String> prereqs = graph.getAdjacencyList()
                    .getOrDefault(nodeId, List.of()).stream()
                    .map(Edge::getTo)
                    .filter(failedSet::contains)
                    .collect(Collectors.toList());

            steps.add(new LiveRecoveryPlan.RecoveryStep(
                    i + 1,
                    nodeId,
                    node.getRestartCost(),
                    round(cumulativeCost),
                    prereqs
            ));
        }

        return new LiveRecoveryPlan(
                steps, failedNodes, round(cumulativeCost),
                steps.size(), false
        );
    }

    // ━━━━━━━━━━━━━━━━━━━ HELPERS ━━━━━━━━━━━━━━━━━━━

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
