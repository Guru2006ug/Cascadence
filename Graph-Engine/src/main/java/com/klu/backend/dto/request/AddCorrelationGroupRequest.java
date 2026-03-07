package com.klu.backend.dto.request;

import java.util.List;

/**
 * Request body for adding a failure correlation group.
 *
 * @param groupId           unique group identifier
 * @param nodeIds           list of correlated service IDs
 * @param correlationFactor probability that group members co-fail [0.0, 1.0]
 */
public record AddCorrelationGroupRequest(
        String groupId,
        List<String> nodeIds,
        double correlationFactor
) {}
