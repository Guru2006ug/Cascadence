import { api } from "../api/apiClient";

export const optimizationService = {
  getDijkstra: (source) =>
    api.get("/optimization/dijkstra", { params: { source } }),

  getClusters: () => api.get("/optimization/clusters"),

  getMST: () => api.get("/optimization/mst"),
};
