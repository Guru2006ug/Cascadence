package com.klu.backend.dto.request;

import java.util.List;

/**
 * Request body for bulk-loading an entire service graph.
 *
 * Example JSON:
 * {
 *   "services": [
 *     {"id": "DB", "restartCost": 10},
 *     {"id": "Auth", "restartCost": 5}
 *   ],
 *   "dependencies": [
 *     {"from": "Auth", "to": "DB", "failureProbability": 0.9, "infraCost": 2.0}
 *   ]
 * }
 */
public record GraphLoadRequest(
        List<ServiceInput> services,
        List<DependencyInput> dependencies
) {
    public record ServiceInput(String id, double restartCost,
                                Double recoveryTime, Double importanceWeight) {
        public ServiceInput {
            if (recoveryTime == null) recoveryTime = 0.0;
            if (importanceWeight == null || importanceWeight <= 0) importanceWeight = 1.0;
        }
    }

    public record DependencyInput(String from, String to,
                                   double failureProbability, double infraCost,
                                   Double propagationDelay) {
        public DependencyInput {
            if (propagationDelay == null) propagationDelay = 0.0;
        }
    }
}
