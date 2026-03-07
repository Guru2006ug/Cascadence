package com.klu.backend.dto.response;

import java.util.List;

/**
 * Response representing the complete state of the service graph.
 *
 * @param services        all registered services with their metadata
 * @param dependencies    all dependency edges
 * @param serviceCount    total number of services
 * @param dependencyCount total number of dependencies
 */
public record GraphStateResponse(
        List<ServiceInfo> services,
        List<DependencyInfo> dependencies,
        int serviceCount,
        int dependencyCount
) {
    /**
     * Service node summary.
     */
    public record ServiceInfo(String id, double restartCost, String state,
                               double recoveryTime, double importanceWeight) {}

    /**
     * Dependency edge summary.
     */
    public record DependencyInfo(String from, String to, double failureProbability,
                                  double infraCost, double propagationDelay) {}
}
