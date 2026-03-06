package com.klu.backend.dto.request;

/**
 * Request body for adding a new service to the graph.
 *
 * @param id          unique service identifier
 * @param restartCost cost to restart this service
 */
public record AddServiceRequest(String id, double restartCost) {}
