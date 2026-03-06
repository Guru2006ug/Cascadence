package com.klu.backend.dto.request;

/**
 * Request body for adding a dependency edge between two services.
 *
 * @param from                dependent service (depends on 'to')
 * @param to                  dependency service
 * @param failureProbability  probability of failure propagation [0.0, 1.0]
 * @param infraCost           infrastructure cost of this connection
 */
public record AddDependencyRequest(
        String from,
        String to,
        double failureProbability,
        double infraCost
) {}
