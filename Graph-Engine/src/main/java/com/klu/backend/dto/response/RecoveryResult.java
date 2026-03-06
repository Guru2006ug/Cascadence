package com.klu.backend.dto.response;

import java.util.List;

/**
 * Response for recovery planning.
 *
 * @param hasCycle             true if the dependency graph contains a cycle (recovery unsafe)
 * @param restartOrder         safe restart sequence (empty if cycle exists)
 * @param criticalChainLength  length of the longest dependency chain (-1 if cycle exists)
 */
public record RecoveryResult(
        boolean hasCycle,
        List<String> restartOrder,
        int criticalChainLength
) {}
