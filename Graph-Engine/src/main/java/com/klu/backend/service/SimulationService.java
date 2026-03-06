package com.klu.backend.service;

import com.klu.backend.algorithm.DFSUtil;
import com.klu.backend.dto.response.CascadeResult;
import com.klu.backend.exception.GraphValidationException;
import com.klu.backend.model.Graph;
import org.springframework.stereotype.Service;

/**
 * Handles deterministic cascading failure simulation.
 * Delegates to DFSUtil (BFS on reverse graph) and maps results to API DTOs.
 */
@Service
public class SimulationService {

    private final GraphService graphService;
    private final DFSUtil dfsUtil;

    public SimulationService(GraphService graphService, DFSUtil dfsUtil) {
        this.graphService = graphService;
        this.dfsUtil = dfsUtil;
    }

    /**
     * Simulate cascading failure from a given node.
     *
     * @param failedNode the service that initially fails
     * @return cascade result with affected nodes, depth, and impact score
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
                data.affectedCount(),
                graph.getNodeCount()
        );
    }
}
