import { create } from "zustand";
import { graphService } from "../services/graphService";

export const useGraphStore = create((set, get) => ({
  nodes: [],
  edges: [],
  serviceCount: 0,
  dependencyCount: 0,
  selectedNode: null,
  selectedEdge: null,
  loading: false,
  error: null,

  setSelectedNode: (node) => set({ selectedNode: node }),
  setSelectedEdge: (edge) => set({ selectedEdge: edge }),
  clearSelection: () => set({ selectedNode: null, selectedEdge: null }),

  fetchGraph: async () => {
    set({ loading: true, error: null });
    try {
      const res = await graphService.getGraph();
      set({
        nodes: res.data.services || [],
        edges: res.data.dependencies || [],
        serviceCount: res.data.serviceCount,
        dependencyCount: res.data.dependencyCount,
        loading: false,
      });
    } catch (err) {
      set({ error: err.response?.data?.error || "Failed to fetch graph", loading: false });
    }
  },

  addService: async (data) => {
    set({ loading: true, error: null });
    try {
      await graphService.addService(data);
      await get().fetchGraph();
    } catch (err) {
      set({ error: err.response?.data?.error || "Failed to add service", loading: false });
    }
  },

  removeService: async (id) => {
    set({ loading: true, error: null });
    try {
      await graphService.removeService(id);
      set({ selectedNode: null });
      await get().fetchGraph();
    } catch (err) {
      set({ error: err.response?.data?.error || "Failed to remove service", loading: false });
    }
  },

  addDependency: async (data) => {
    set({ loading: true, error: null });
    try {
      await graphService.addDependency(data);
      await get().fetchGraph();
    } catch (err) {
      set({ error: err.response?.data?.error || "Failed to add dependency", loading: false });
    }
  },

  removeDependency: async (from, to) => {
    set({ loading: true, error: null });
    try {
      await graphService.removeDependency(from, to);
      set({ selectedEdge: null });
      await get().fetchGraph();
    } catch (err) {
      set({ error: err.response?.data?.error || "Failed to remove dependency", loading: false });
    }
  },

  updateRestartCost: async (id, cost) => {
    set({ error: null });
    try {
      await graphService.updateRestartCost(id, cost);
      await get().fetchGraph();
    } catch (err) {
      set({ error: err.response?.data?.error || "Failed to update cost" });
    }
  },

  loadGraph: async (data) => {
    set({ loading: true, error: null });
    try {
      await graphService.loadGraph(data);
      await get().fetchGraph();
    } catch (err) {
      set({ error: err.response?.data?.error || "Failed to load graph", loading: false });
    }
  },

  clearGraph: async () => {
    set({ loading: true, error: null });
    try {
      await graphService.clearGraph();
      set({
        nodes: [],
        edges: [],
        serviceCount: 0,
        dependencyCount: 0,
        selectedNode: null,
        selectedEdge: null,
        loading: false,
      });
    } catch (err) {
      set({ error: err.response?.data?.error || "Failed to clear graph", loading: false });
    }
  },

  clearError: () => set({ error: null }),
}));
