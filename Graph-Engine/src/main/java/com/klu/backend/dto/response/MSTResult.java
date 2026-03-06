package com.klu.backend.dto.response;

import java.util.List;

/**
 * Response for Minimum Spanning Tree (infrastructure optimization).
 *
 * @param edges          edges selected for the MST
 * @param totalInfraCost total infrastructure cost of the MST
 * @param edgeCount      number of edges in the MST
 */
public record MSTResult(
        List<MSTEdgeInfo> edges,
        double totalInfraCost,
        int edgeCount
) {
    /**
     * Simplified edge representation for MST output.
     */
    public record MSTEdgeInfo(String from, String to, double infraCost) {}
}
