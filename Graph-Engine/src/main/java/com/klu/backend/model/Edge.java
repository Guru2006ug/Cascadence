package com.klu.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a directed dependency edge in the service graph.
 *
 * Edge from → to means: service 'from' depends on service 'to'.
 * If 'to' fails, the failure propagates to 'from' with the given probability.
 *
 * Fields:
 *   from                — dependent service
 *   to                  — dependency service
 *   failureProbability  — probability of failure propagation [0.0, 1.0]
 *   infraCost           — infrastructure/network cost of this connection
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Edge {

    private String from;
    private String to;
    private double failureProbability;
    private double infraCost;

    /**
     * Creates an edge with default probability 1.0 and infraCost 1.0.
     */
    public Edge(String from, String to) {
        this.from = from;
        this.to = to;
        this.failureProbability = 1.0;
        this.infraCost = 1.0;
    }
}
