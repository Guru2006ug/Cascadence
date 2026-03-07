package com.klu.backend.model;

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
public class Edge {

    private String from;
    private String to;
    private double failureProbability;
    private double infraCost;
    private double propagationDelay;  // seconds for failure to propagate across this edge (0 = instant)

    /**
     * Creates an edge with all fields.
     */
    public Edge(String from, String to, double failureProbability, double infraCost, double propagationDelay) {
        this.from = from;
        this.to = to;
        this.failureProbability = failureProbability;
        this.infraCost = infraCost;
        this.propagationDelay = propagationDelay;
    }

    /**
     * Backward-compatible constructor (propagation delay defaults to 0).
     */
    public Edge(String from, String to, double failureProbability, double infraCost) {
        this(from, to, failureProbability, infraCost, 0.0);
    }

    /**
     * Creates an edge with default probability 1.0, infraCost 1.0, and delay 0.
     */
    public Edge(String from, String to) {
        this(from, to, 1.0, 1.0, 0.0);
    }
}
