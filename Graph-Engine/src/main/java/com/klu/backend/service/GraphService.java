package com.klu.backend.service;

import com.klu.backend.dto.request.AddCorrelationGroupRequest;
import com.klu.backend.dto.request.AddDependencyRequest;
import com.klu.backend.dto.request.AddServiceRequest;
import com.klu.backend.dto.request.GraphLoadRequest;
import com.klu.backend.dto.response.GraphStateResponse;
import com.klu.backend.model.Graph;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the in-memory service dependency graph.
 * Handles CRUD operations: add/remove services, add/remove dependencies, cost updates.
 *
 * The Graph instance is a singleton held by this service.
 * All other services access the graph through GraphService.getGraph().
 */
@Service
public class GraphService {

    private final Graph graph = new Graph();

    /** Returns the underlying graph instance (for algorithm services). */
    public Graph getGraph() {
        return graph;
    }

    // ──────────────────────────── Service CRUD ────────────────────────────

    public void addService(AddServiceRequest request) {
        graph.addNode(request.id(), request.restartCost(),
                request.recoveryTime(), request.importanceWeight());
    }

    public void removeService(String id) {
        graph.removeNode(id);
    }

    // ──────────────────────────── Dependency CRUD ────────────────────────────

    public void addDependency(AddDependencyRequest request) {
        graph.addEdge(
                request.from(),
                request.to(),
                request.failureProbability(),
                request.infraCost(),
                request.propagationDelay()
        );
    }

    public void removeDependency(String from, String to) {
        graph.removeEdge(from, to);
    }

    // ──────────────────────────── Updates ────────────────────────────

    public void updateRestartCost(String id, double cost) {
        graph.updateRestartCost(id, cost);
    }

    // ──────────────────────────── Bulk Operations ────────────────────────────

    /**
     * Load an entire graph from a structured request. Clears existing graph first.
     */
    public void loadGraph(GraphLoadRequest request) {
        graph.clear();
        for (GraphLoadRequest.ServiceInput svc : request.services()) {
            graph.addNode(svc.id(), svc.restartCost(),
                    svc.recoveryTime(), svc.importanceWeight());
        }
        for (GraphLoadRequest.DependencyInput dep : request.dependencies()) {
            graph.addEdge(dep.from(), dep.to(), dep.failureProbability(),
                    dep.infraCost(), dep.propagationDelay());
        }
    }

    public void clearGraph() {
        graph.clear();
    }

    // ──────────────────────────── Correlation Groups ────────────────────────────

    public void addCorrelationGroup(AddCorrelationGroupRequest request) {
        graph.addCorrelationGroup(request.groupId(), request.nodeIds(), request.correlationFactor());
    }

    public void removeCorrelationGroup(String groupId) {
        graph.removeCorrelationGroup(groupId);
    }

    // ──────────────────────────── Query ────────────────────────────

    /**
     * Returns a complete snapshot of the current graph state.
     */
    public GraphStateResponse getGraphState() {
        List<GraphStateResponse.ServiceInfo> services = graph.getNodes().values().stream()
                .map(n -> new GraphStateResponse.ServiceInfo(
                        n.getId(), n.getRestartCost(), n.getState().name(),
                        n.getRecoveryTime(), n.getImportanceWeight()))
                .collect(Collectors.toList());

        List<GraphStateResponse.DependencyInfo> dependencies = graph.getAllEdges().stream()
                .map(e -> new GraphStateResponse.DependencyInfo(
                        e.getFrom(), e.getTo(), e.getFailureProbability(),
                        e.getInfraCost(), e.getPropagationDelay()))
                .collect(Collectors.toList());

        return new GraphStateResponse(services, dependencies, services.size(), dependencies.size());
    }
}
