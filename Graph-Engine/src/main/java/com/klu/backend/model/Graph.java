package com.klu.backend.model;

import com.klu.backend.exception.GraphValidationException;

import java.util.*;

/**
 * Core directed weighted graph representing a microservice architecture.
 *
 * Internal structures (always kept in sync):
 *   nodes           — Map of serviceId → ServiceNode
 *   adjacencyList   — Forward adjacency: from → List<Edge>
 *   reverseAdjList  — Reverse adjacency: to → List<Edge>  (for cascade traversal)
 *   inDegree        — Number of incoming edges per node    (for topological sort)
 *
 * All mutation methods are synchronized for thread-safety readiness.
 */
public class Graph {

    private final Map<String, ServiceNode> nodes = new LinkedHashMap<>();
    private final Map<String, List<Edge>> adjacencyList = new LinkedHashMap<>();
    private final Map<String, List<Edge>> reverseAdjList = new LinkedHashMap<>();
    private final Map<String, Integer> inDegree = new HashMap<>();

    // ──────────────────────────── Node Operations ────────────────────────────

    /**
     * Add a service node. O(1)
     * Initializes adjacency lists and inDegree entry.
     */
    public synchronized void addNode(String id, double restartCost) {
        if (id == null || id.isBlank()) {
            throw new GraphValidationException("Service ID cannot be null or blank");
        }
        if (nodes.containsKey(id)) {
            throw new GraphValidationException("Service already exists: " + id);
        }
        nodes.put(id, new ServiceNode(id, restartCost));
        adjacencyList.putIfAbsent(id, new ArrayList<>());
        reverseAdjList.putIfAbsent(id, new ArrayList<>());
        inDegree.putIfAbsent(id, 0);
    }

    /**
     * Remove a service node and all its edges. O(deg(node))
     */
    public synchronized void removeNode(String id) {
        validateNodeExists(id);

        // Remove outgoing edges: for each edge (id → X), clean reverseAdjList[X] and inDegree[X]
        for (Edge edge : new ArrayList<>(adjacencyList.getOrDefault(id, List.of()))) {
            reverseAdjList.getOrDefault(edge.getTo(), new ArrayList<>())
                    .removeIf(e -> e.getFrom().equals(id));
            inDegree.computeIfPresent(edge.getTo(), (k, v) -> Math.max(0, v - 1));
        }

        // Remove incoming edges: for each edge (X → id), clean adjacencyList[X]
        for (Edge edge : new ArrayList<>(reverseAdjList.getOrDefault(id, List.of()))) {
            adjacencyList.getOrDefault(edge.getFrom(), new ArrayList<>())
                    .removeIf(e -> e.getTo().equals(id));
        }

        adjacencyList.remove(id);
        reverseAdjList.remove(id);
        inDegree.remove(id);
        nodes.remove(id);
    }

    // ──────────────────────────── Edge Operations ────────────────────────────

    /**
     * Add a directed dependency edge. O(1) amortized.
     * Validates: both nodes exist, probability ∈ [0,1], no self-loop, no duplicate.
     */
    public synchronized void addEdge(String from, String to, double probability, double infraCost) {
        validateNodeExists(from);
        validateNodeExists(to);
        if (from.equals(to)) {
            throw new GraphValidationException("Self-dependency not allowed: " + from);
        }
        if (probability < 0 || probability > 1) {
            throw new GraphValidationException("Failure probability must be in [0, 1]: " + probability);
        }

        boolean duplicate = adjacencyList.get(from).stream()
                .anyMatch(e -> e.getTo().equals(to));
        if (duplicate) {
            throw new GraphValidationException("Dependency already exists: " + from + " → " + to);
        }

        Edge edge = new Edge(from, to, probability, infraCost);
        adjacencyList.get(from).add(edge);
        reverseAdjList.get(to).add(edge);
        inDegree.merge(to, 1, Integer::sum);
    }

    /**
     * Remove a directed dependency edge. O(deg(from))
     */
    public synchronized void removeEdge(String from, String to) {
        validateNodeExists(from);
        validateNodeExists(to);

        boolean removed = adjacencyList.get(from).removeIf(e -> e.getTo().equals(to));
        if (!removed) {
            throw new GraphValidationException("Dependency not found: " + from + " → " + to);
        }
        reverseAdjList.get(to).removeIf(e -> e.getFrom().equals(from));
        inDegree.computeIfPresent(to, (k, v) -> Math.max(0, v - 1));
    }

    // ──────────────────────────── Update Operations ────────────────────────────

    /**
     * Update restart cost for a service.
     */
    public synchronized void updateRestartCost(String id, double cost) {
        validateNodeExists(id);
        nodes.get(id).setRestartCost(cost);
    }

    /**
     * Update failure probability on an existing edge.
     */
    public synchronized void updateEdgeProbability(String from, String to, double probability) {
        validateNodeExists(from);
        validateNodeExists(to);
        if (probability < 0 || probability > 1) {
            throw new GraphValidationException("Failure probability must be in [0, 1]: " + probability);
        }
        Edge edge = adjacencyList.get(from).stream()
                .filter(e -> e.getTo().equals(to))
                .findFirst()
                .orElseThrow(() -> new GraphValidationException(
                        "Dependency not found: " + from + " → " + to));
        edge.setFailureProbability(probability);
    }

    /**
     * Clear all nodes and edges.
     */
    public synchronized void clear() {
        nodes.clear();
        adjacencyList.clear();
        reverseAdjList.clear();
        inDegree.clear();
    }

    // ──────────────────────────── Getters (Unmodifiable Views) ────────────────────────────

    public Map<String, ServiceNode> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }

    public Map<String, List<Edge>> getAdjacencyList() {
        return Collections.unmodifiableMap(adjacencyList);
    }

    public Map<String, List<Edge>> getReverseAdjList() {
        return Collections.unmodifiableMap(reverseAdjList);
    }

    /** Returns a mutable COPY of inDegree (safe for algorithms to modify). */
    public Map<String, Integer> getInDegree() {
        return new HashMap<>(inDegree);
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getEdgeCount() {
        return adjacencyList.values().stream().mapToInt(List::size).sum();
    }

    public boolean containsNode(String id) {
        return nodes.containsKey(id);
    }

    /** Returns a flat list of all edges in the graph. */
    public List<Edge> getAllEdges() {
        List<Edge> all = new ArrayList<>();
        adjacencyList.values().forEach(all::addAll);
        return all;
    }

    // ──────────────────────────── Validation ────────────────────────────

    private void validateNodeExists(String id) {
        if (!nodes.containsKey(id)) {
            throw new GraphValidationException("Service not found: " + id);
        }
    }
}
