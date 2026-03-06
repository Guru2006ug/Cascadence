package com.klu.backend.algorithm;

import java.util.*;

/**
 * Disjoint Set Union (Union-Find) with path compression and union by rank.
 *
 * Used for:
 *   - Cluster detection (connected components)
 *   - Kruskal's MST cycle avoidance
 *
 * Complexity: Nearly O(1) amortized per find/union (inverse Ackermann).
 *
 * Stateful — create a new instance per usage.
 */
public class DSU {

    private final Map<String, String> parent = new HashMap<>();
    private final Map<String, Integer> rank = new HashMap<>();

    /**
     * Initialize DSU with a set of node IDs.
     * Each node starts as its own parent with rank 0.
     */
    public DSU(Set<String> nodes) {
        for (String node : nodes) {
            parent.put(node, node);
            rank.put(node, 0);
        }
    }

    /**
     * Find the root representative of the set containing x.
     * Uses path compression for amortized performance.
     */
    public String find(String x) {
        if (!parent.get(x).equals(x)) {
            parent.put(x, find(parent.get(x)));  // path compression
        }
        return parent.get(x);
    }

    /**
     * Merge the sets containing x and y.
     * Uses union by rank to keep tree balanced.
     */
    public void union(String x, String y) {
        String rootX = find(x);
        String rootY = find(y);
        if (rootX.equals(rootY)) return;

        int rankX = rank.get(rootX);
        int rankY = rank.get(rootY);

        if (rankX < rankY) {
            parent.put(rootX, rootY);
        } else if (rankX > rankY) {
            parent.put(rootY, rootX);
        } else {
            parent.put(rootY, rootX);
            rank.put(rootX, rankX + 1);
        }
    }

    /**
     * Check if two nodes belong to the same set.
     */
    public boolean connected(String x, String y) {
        return find(x).equals(find(y));
    }

    /**
     * Returns all clusters (connected components) as a list of lists.
     */
    public List<List<String>> getClusters() {
        Map<String, List<String>> clusterMap = new LinkedHashMap<>();
        for (String node : parent.keySet()) {
            String root = find(node);
            clusterMap.computeIfAbsent(root, k -> new ArrayList<>()).add(node);
        }
        return new ArrayList<>(clusterMap.values());
    }
}
