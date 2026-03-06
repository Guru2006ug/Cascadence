package com.klu.backend.model;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class ServiceNode {

    private String id;
    private double restartCost;
    private ServiceState state;

    /**
     * Creates a HEALTHY service node with the given id and restart cost.
     */
    public ServiceNode(String id, double restartCost) {
        this.id = id;
        this.restartCost = restartCost;
        this.state = ServiceState.HEALTHY;
    }
}
