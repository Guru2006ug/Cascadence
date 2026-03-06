package com.klu.backend.dto.request;

import java.util.List;

/**
 * Request body for what-if analysis.
 *
 * Allows the user to specify modifications to the current graph
 * and see how system risk changes before vs. after.
 *
 * Supported modifications:
 *   - Update edge failure probabilities
 *   - Add new edges
 *   - Remove existing edges
 *
 * Example JSON:
 * {
 *   "failedNode": "DB",
 *   "iterations": 1000,
 *   "edgeUpdates": [
 *     {"from": "Auth", "to": "DB", "newProbability": 0.3}
 *   ],
 *   "edgeAdditions": [
 *     {"from": "Payment", "to": "DB", "probability": 0.5, "infraCost": 2.0}
 *   ],
 *   "edgeRemovals": [
 *     {"from": "API", "to": "DB"}
 *   ]
 * }
 */
public record WhatIfRequest(
        String failedNode,
        int iterations,
        List<EdgeUpdate> edgeUpdates,
        List<EdgeAddition> edgeAdditions,
        List<EdgeRemoval> edgeRemovals
) {
    public record EdgeUpdate(String from, String to, double newProbability) {}
    public record EdgeAddition(String from, String to, double probability, double infraCost) {}
    public record EdgeRemoval(String from, String to) {}
}
