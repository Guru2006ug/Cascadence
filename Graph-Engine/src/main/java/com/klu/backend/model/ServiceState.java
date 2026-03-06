package com.klu.backend.model;

/**
 * Represents the health state of a microservice.
 * State transitions:
 *   HEALTHY → FAILED     (on cascade failure)
 *   FAILED → RECOVERING  (on recovery start)
 *   RECOVERING → HEALTHY  (on recovery complete)
 */
public enum ServiceState {
    HEALTHY,
    FAILED,
    RECOVERING
}
