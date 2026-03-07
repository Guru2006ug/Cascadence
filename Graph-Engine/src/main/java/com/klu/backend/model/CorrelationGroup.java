package com.klu.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a correlated failure group.
 *
 * When any node in this group fails, all other group members
 * have a probability = correlationFactor of also failing,
 * regardless of direct edge connections.
 *
 * Example: [Auth, Order, Inventory] with correlationFactor 0.7
 * → if Auth fails, Order and Inventory each have 70% chance of correlated failure.
 */
@Data
@NoArgsConstructor
public class CorrelationGroup {

    private String groupId;
    private List<String> nodeIds;
    private double correlationFactor;  // [0.0, 1.0]

    public CorrelationGroup(String groupId, List<String> nodeIds, double correlationFactor) {
        this.groupId = groupId;
        this.nodeIds = nodeIds;
        this.correlationFactor = correlationFactor;
    }
}
