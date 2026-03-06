package com.klu.backend.dto.response;

import java.util.List;

/**
 * Response for cluster detection (connected components via DSU).
 *
 * @param clusters     list of service groups (each group is an independent cluster)
 * @param clusterCount number of independent clusters
 */
public record ClusterResult(
        List<List<String>> clusters,
        int clusterCount
) {}
