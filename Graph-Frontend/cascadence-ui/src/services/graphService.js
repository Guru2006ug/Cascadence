import { api } from "../api/apiClient";

export const graphService = {
  getGraph: () => api.get("/graph"),

  addService: (data) => api.post("/graph/services", data),

  removeService: (id) => api.delete(`/graph/services/${encodeURIComponent(id)}`),

  addDependency: (data) => api.post("/graph/dependencies", data),

  removeDependency: (from, to) =>
    api.delete("/graph/dependencies", { params: { from, to } }),

  updateRestartCost: (id, cost) =>
    api.put(`/graph/services/${encodeURIComponent(id)}/cost`, null, { params: { cost } }),

  loadGraph: (data) => api.post("/graph/load", data),

  clearGraph: () => api.delete("/graph"),

  addCorrelationGroup: (data) => api.post("/graph/correlation-groups", data),

  removeCorrelationGroup: (groupId) =>
    api.delete(`/graph/correlation-groups/${encodeURIComponent(groupId)}`),
};
