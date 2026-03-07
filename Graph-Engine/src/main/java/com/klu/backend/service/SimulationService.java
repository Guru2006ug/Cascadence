package com.klu.backend.service;

import com.klu.backend.algorithm.DFSUtil;
import com.klu.backend.algorithm.TemporalCascadeUtil;
import com.klu.backend.dto.response.CascadeResult;
import com.klu.backend.dto.response.TemporalCascadeResult;
import com.klu.backend.exception.GraphValidationException;
import com.klu.backend.model.Graph;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Handles cascading failure simulation.
 * Delegates to DFSUtil (deterministic BFS) and TemporalCascadeUtil (time-based PQ).
 */
@Service
public class SimulationService {

    private final GraphService graphService;
    private final DFSUtil dfsUtil;
    private final TemporalCascadeUtil temporalCascadeUtil;

    public SimulationService(GraphService graphService, DFSUtil dfsUtil,
                              TemporalCascadeUtil temporalCascadeUtil) {
        this.graphService = graphService;
        this.dfsUtil = dfsUtil;
        this.temporalCascadeUtil = temporalCascadeUtil;
    }

    /**
     * Simulate deterministic cascading failure from a given node.
     */
    public CascadeResult simulateCascade(String failedNode) {
        Graph graph = graphService.getGraph();

        if (!graph.containsNode(failedNode)) {
            throw new GraphValidationException("Service not found: " + failedNode);
        }

        DFSUtil.CascadeData data = dfsUtil.simulateCascade(failedNode, graph);

        return new CascadeResult(
                data.failedNode(),
                data.affectedNodes(),
                data.depthMap(),
                data.cascadeDepth(),
                data.impactScore(),
                data.weightedImpactScore(),
                data.affectedCount(),
                graph.getNodeCount()
        );
    }

    /**
     * Simulate time-based cascade from a given node.
     * Uses priority queue ordered by propagation delay.
     */
    public TemporalCascadeResult simulateTemporalCascade(String failedNode) {
        Graph graph = graphService.getGraph();

        if (!graph.containsNode(failedNode)) {
            throw new GraphValidationException("Service not found: " + failedNode);
        }

        TemporalCascadeUtil.TemporalCascadeData data =
                temporalCascadeUtil.simulate(failedNode, graph);

        return new TemporalCascadeResult(
                data.failedNode(),
                data.timeline().stream()
                        .map(e -> new TemporalCascadeResult.TemporalEvent(
                                e.nodeId(), e.failureTime(), e.cascadeLevel(), e.triggeredBy()))
                        .collect(Collectors.toList()),
                data.cascadeDuration(),
                data.timeToSystemCollapse(),
                data.peakFailureMoment(),
                data.peakConcurrentFailures(),
                data.totalAffected(),
                data.totalNodes(),
                data.impactScore(),
                data.weightedImpactScore()
        );
    }
}
