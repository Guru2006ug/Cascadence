package com.klu.backend.service;

import com.klu.backend.algorithm.TopoSortUtil;
import com.klu.backend.dto.response.RecoveryResult;
import com.klu.backend.model.Graph;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles recovery planning:
 *   - Cycle detection (DFS coloring)
 *   - Safe restart order (topological sort, reversed)
 *   - Critical chain length (longest path in DAG)
 */
@Service
public class RecoveryService {

    private final GraphService graphService;
    private final TopoSortUtil topoSortUtil;

    public RecoveryService(GraphService graphService, TopoSortUtil topoSortUtil) {
        this.graphService = graphService;
        this.topoSortUtil = topoSortUtil;
    }

    /**
     * Compute the full recovery plan.
     *
     * The restart order is the REVERSE of topological sort:
     *   Topo order:    [Payment, API, Auth, DB]  (dependents first)
     *   Restart order: [DB, Auth, API, Payment]  (dependencies first)
     *
     * If the graph has a cycle, restart order is empty and cycle flag is set.
     */
    public RecoveryResult computeRecoveryPlan() {
        Graph graph = graphService.getGraph();

        TopoSortUtil.TopologicalSortData sortData = topoSortUtil.topologicalSort(graph);
        int criticalChain = topoSortUtil.longestPath(graph);

        List<String> restartOrder;
        if (sortData.hasCycle()) {
            restartOrder = List.of();
        } else {
            // Reverse topo order = restart from leaf dependencies first
            restartOrder = new ArrayList<>(sortData.order());
            Collections.reverse(restartOrder);
        }

        return new RecoveryResult(
                sortData.hasCycle(),
                restartOrder,
                criticalChain
        );
    }

    /**
     * Quick cycle check without computing full recovery plan.
     */
    public boolean checkCycle() {
        return topoSortUtil.hasCycle(graphService.getGraph());
    }
}
