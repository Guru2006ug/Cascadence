package com.klu.backend.dto.request;

/**
 * Tier 3 — Request to change a service node's health state.
 *
 * Valid states: "HEALTHY", "FAILED", "RECOVERING"
 *
 * @param state the target state string (case-insensitive)
 */
public record NodeStateRequest(
        String state
) {}
