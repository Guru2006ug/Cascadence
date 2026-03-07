package com.klu.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a microservice node in the dependency graph.
 *
 * Fields:
 *   id          — unique service identifier
 *   restartCost — cost to restart this service
 *   state       — current health state (HEALTHY | FAILED | RECOVERING)
 */
@Data
@NoArgsConstructor
public class ServiceNode {

    private String id;
    private double restartCost;
    private ServiceState state;
    private double recoveryTime;      // seconds to recover after failure (0 = instant)
    private double importanceWeight;  // service criticality weight (default 1.0)

    /**
     * Creates a HEALTHY service node with the given id and restart cost.
     * Recovery time defaults to 0, importance weight defaults to 1.0.
     */
    public ServiceNode(String id, double restartCost) {
        this.id = id;
        this.restartCost = restartCost;
        this.state = ServiceState.HEALTHY;
        this.recoveryTime = 0.0;
        this.importanceWeight = 1.0;
    }

    /**
     * Full constructor with all fields.
     */
    public ServiceNode(String id, double restartCost, ServiceState state,
                       double recoveryTime, double importanceWeight) {
        this.id = id;
        this.restartCost = restartCost;
        this.state = state;
        this.recoveryTime = recoveryTime;
        this.importanceWeight = importanceWeight > 0 ? importanceWeight : 1.0;
    }

    /**
     * Creates a HEALTHY node with recovery time and importance weight.
     */
    public ServiceNode(String id, double restartCost, double recoveryTime, double importanceWeight) {
        this(id, restartCost, ServiceState.HEALTHY, recoveryTime,
             importanceWeight > 0 ? importanceWeight : 1.0);
    }
}
