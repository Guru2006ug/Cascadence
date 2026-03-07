package com.klu.backend.dto.request;

/**
 * Request body for adding a new service to the graph.
 *
 * @param id               unique service identifier
 * @param restartCost      cost to restart this service
 * @param recoveryTime     time to recover in seconds (default 0 = instant)
 * @param importanceWeight criticality weight (default 1.0)
 */
public record AddServiceRequest(String id, double restartCost,
                                 Double recoveryTime, Double importanceWeight) {
    public AddServiceRequest {
        if (recoveryTime == null) recoveryTime = 0.0;
        if (importanceWeight == null || importanceWeight <= 0) importanceWeight = 1.0;
    }
}
